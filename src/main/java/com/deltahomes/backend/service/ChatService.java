package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.chat.ChatDtos;
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

    @Transactional
    public Conversation getOrCreateConversation(User userOne, User userTwo) {
        return conversationRepository.findByUserOneIdAndUserTwoId(userOne.getId(), userTwo.getId())
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setUserOne(userOne);
                    conversation.setUserTwo(userTwo);
                    return conversationRepository.save(conversation);
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
    public Message sendMessage(User sender, UUID conversationId, ChatDtos.CreateMessageRequest request) {
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
        return saved;
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        assertParticipant(conversation, user);
        return conversation;
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

    public PaginatedResponse<ConversationSummary> indexConversations(User user, Pageable pageable) {
        Page<ConversationSummary> page = conversationRepository.searchIndex(user.getId(), PageUtils.normalizeSort(pageable))
            .map(this::toConversationSummary);
        return PaginatedResponse.from(page);
    }

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

    private static boolean isParticipant(Conversation conversation, User user) {
        return conversation.getUserOne().getId().equals(user.getId())
                || conversation.getUserTwo().getId().equals(user.getId());
    }

    private static void assertParticipant(Conversation conversation, User user) {
        if (!isParticipant(conversation, user)) {
            throw new BusinessException("You are not a participant in this conversation");
        }
    }

    private ConversationSummary toConversationSummary(Conversation c) {
        // Determine the other user based on which side is the current user
        // Note: caller doesn't pass userId here, so we return both and let the client decide
        return new ConversationSummary() {
            @Override public UUID getId() { return c.getId(); }
            @Override public String getLastMessagePreview() { return c.getLastMessagePreview(); }
            @Override public java.time.OffsetDateTime getUpdatedAt() { return c.getUpdatedAt(); }
            @Override public UUID getOtherUserId() { return c.getUserOne() != null ? c.getUserOne().getId() : null; }
            @Override public String getOtherUserName() { return c.getUserOne() != null ? c.getUserOne().getName() : null; }
        };
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
