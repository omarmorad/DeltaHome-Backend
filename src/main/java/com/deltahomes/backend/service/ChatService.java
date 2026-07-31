package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.ConversationRepository;
import com.deltahomes.backend.repository.MessageRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
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

    @Transactional
    public Message sendMessage(Conversation conversation, User sender, Message message) {
        message.setConversation(conversation);
        message.setSender(sender);
        return messageRepository.save(message);
    }

    public PaginatedResponse<ConversationSummary> indexConversations(User user, Pageable pageable) {
        Page<ConversationSummary> page = conversationRepository.searchIndex(user.getId(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<MessageSummary> getConversationMessages(UUID conversationId, String q,
                                                                     Pageable pageable) {
        Page<MessageSummary> page = messageRepository.searchIndex(
                conversationId, q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }
}
