package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.ConversationRepository;
import com.deltahomes.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<Message> getConversationMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
