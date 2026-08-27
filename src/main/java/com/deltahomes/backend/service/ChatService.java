package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.chat.ChatDtos;
import com.deltahomes.backend.dto.chat.ChatDtos.ConversationResponse;
import com.deltahomes.backend.dto.chat.ChatDtos.MessageResponse;
import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.enums.MessageType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.ConversationRepository;
import com.deltahomes.backend.repository.MessageRepository;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Finds or creates the 1:1 conversation between two users. The pair is
     * normalized (lower UUID always stored as {@code userOne}) so both lookup
     * directions hit the same row, and a DB unique constraint backs the
     * check-then-insert against concurrent duplicates.
     */
    @Transactional
    public Conversation getOrCreateConversation(User userOne, User userTwo) {
        User first = normalizeFirst(userOne, userTwo);
        User second = first.getId().equals(userOne.getId()) ? userTwo : userOne;

        return conversationRepository.findByUserOneIdAndUserTwoId(first.getId(), second.getId())
                .orElseGet(() -> {
                    try {
                        Conversation conversation = new Conversation();
                        conversation.setUserOne(first);
                        conversation.setUserTwo(second);
                        return conversationRepository.saveAndFlush(conversation);
                    } catch (DataIntegrityViolationException e) {
                        // Lost a race against a concurrent create — reuse the winner.
                        return conversationRepository
                                .findByUserOneIdAndUserTwoId(first.getId(), second.getId())
                                .orElseThrow(() -> e);
                    }
                });
    }

    /** Creates a direct (1:1) conversation between the authenticated user and one participant. */
    @Transactional
    public Conversation createConversation(User user, List<UUID> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new BusinessException("participantIds is required");
        }
        if (participantIds.size() != 1) {
            throw new BusinessException("Conversations are currently 1:1 — provide exactly one participant");
        }
        UUID otherId = participantIds.get(0);
        if (otherId.equals(user.getId())) {
            throw new BusinessException("Cannot start a conversation with yourself");
        }
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", otherId));
        return getOrCreateConversation(user, other);
    }

    /** Sends a message, verifying that the sender is a participant of the conversation. */
    @Transactional
    public MessageResponse sendMessage(User sender, UUID conversationId, ChatDtos.CreateMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!isParticipant(conversation, sender)) {
            throw new BusinessException("You are not a participant in this conversation");
        }

        boolean hasContent = request.content() != null && !request.content().isBlank();
        boolean hasMedia = request.mediaUrl() != null && !request.mediaUrl().isBlank();
        if (!hasContent && !hasMedia) {
            throw new BusinessException("Message must have content or media");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setType(request.type() == null ? MessageType.TEXT : request.type());
        message.setTextBody(request.content());
        message.setMediaUrl(request.mediaUrl());
        message.setPayload(request.payload());
        Message saved = messageRepository.save(message);

        conversation.setLastMessagePreview(truncate(request.content(), 255));
        conversationRepository.save(conversation);
        return toMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        assertParticipant(conversation, user);
        return toConversationResponse(conversation, user.getId());
    }

    /** Marks the conversation as read by the given participant. */
    @Transactional
    public void markRead(User user, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!isParticipant(conversation, user)) {
            throw new BusinessException("You are not a participant in this conversation");
        }
        if (conversation.getUserOne().getId().equals(user.getId())) {
            conversation.setLastSeenUserOne(user.getId());
        } else {
            conversation.setLastSeenUserTwo(user.getId());
        }
        conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ConversationSummary> indexConversations(User user, Pageable pageable) {
        Page<ConversationSummary> page = conversationRepository.searchIndex(
                user.getId(), PageUtils.normalizeSort(pageable))
            .map(c -> toConversationSummary(c, user.getId()));
        return PaginatedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<MessageSummary> getConversationMessages(UUID conversationId, User user,
                                                                     String q, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        assertParticipant(conversation, user);
        Page<MessageSummary> page = messageRepository.searchIndex(
                conversationId, q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable))
            .map(this::toMessageSummary);
        return PaginatedResponse.from(page);
    }

    // ---------- Helpers ----------

    /**
     * Canonical ordering: the user with the lexicographically smaller UUID is
     * stored as {@code userOne}, guaranteeing a single row per unordered pair.
     */
    private static User normalizeFirst(User a, User b) {
        return a.getId().compareTo(b.getId()) <= 0 ? a : b;
    }

    private static User otherUserOf(Conversation c, UUID viewerId) {
        return c.getUserOne().getId().equals(viewerId) ? c.getUserTwo() : c.getUserOne();
    }

    private static boolean isParticipant(Conversation conversation, User user) {
        return conversation.getUserOne().getId().equals(user.getId())
                || conversation.getUserTwo().getId().equals(user.getId());
    }

    private static void assertParticipant(Conversation conversation, User user) {
        if (!isParticipant(conversation, user)) {
            throw new BusinessException("You are not a participant in this conversation");
        }
    }

    private static boolean hasMarkedRead(Conversation c, UUID viewerId) {
        return c.getUserOne().getId().equals(viewerId)
                ? c.getLastSeenUserOne() != null
                : c.getLastSeenUserTwo() != null;
    }

    private ConversationSummary toConversationSummary(Conversation c, UUID viewerId) {
        User other = otherUserOf(c, viewerId);
        return new ConversationSummary() {
            @Override public UUID getId() { return c.getId(); }
            @Override public String getLastMessagePreview() { return c.getLastMessagePreview(); }
            @Override public java.time.OffsetDateTime getUpdatedAt() { return c.getUpdatedAt(); }
            @Override public UUID getOtherUserId() { return other != null ? other.getId() : null; }
            @Override public String getOtherUserName() { return other != null ? other.getName() : null; }
        };
    }

    private ConversationResponse toConversationResponse(Conversation c, UUID viewerId) {
        User other = otherUserOf(c, viewerId);
        return new ConversationResponse(
                c.getId(),
                c.getLastMessagePreview(),
                c.getUpdatedAt(),
                other != null ? new ConversationResponse.OtherUserInfo(
                        other.getId(), other.getName(), other.getPhotoUrl()) : null,
                hasMarkedRead(c, viewerId) ? viewerId : null,
                c.getLastMessagePreview() != null && !hasMarkedRead(c, viewerId)
        );
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation() != null ? m.getConversation().getId() : null,
                m.getSender() != null ? m.getSender().getId() : null,
                m.getSender() != null ? m.getSender().getName() : null,
                m.getType(),
                m.getTextBody(),
                m.getMediaUrl(),
                m.getPayload(),
                m.getCreatedAt()
        );
    }

    private MessageSummary toMessageSummary(Message m) {
        return new MessageSummary() {
            @Override public UUID getId() { return m.getId(); }
            @Override public String getType() { return m.getType() != null ? m.getType().name() : null; }
            @Override public String getTextBody() { return m.getTextBody(); }
            @Override public String getMediaUrl() { return m.getMediaUrl(); }
            @Override public String getPayload() { return m.getPayload(); }
            @Override public String getSenderName() { return m.getSender() != null ? m.getSender().getName() : null; }
            @Override public java.time.OffsetDateTime getCreatedAt() { return m.getCreatedAt(); }
        };
    }

    private static String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
