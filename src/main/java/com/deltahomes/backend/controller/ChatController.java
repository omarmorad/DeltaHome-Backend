package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.auth.AuthDtos;
import com.deltahomes.backend.dto.chat.ChatDtos;
import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.ChatService;
import com.deltahomes.backend.service.UserContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Chat endpoints. Exposed under both {@code /api/v1/chat/...} (specification
 * path) and {@code /api/v1/...} (legacy path) for backward compatibility.
 */
@RestController
@RequestMapping({"/api/v1/chat", "/api/v1"})
public class ChatController {

    private final ChatService chatService;
    private final UserContext userContext;

    public ChatController(ChatService chatService, UserContext userContext) {
        this.chatService = chatService;
        this.userContext = userContext;
    }

    @GetMapping("/conversations")
    public ResponseEntity<PaginatedResponse<ConversationSummary>> index(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(chatService.indexConversations(user, pageable));
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChatDtos.CreateConversationRequest request) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createConversation(user, request.participantIds()));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversation(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(chatService.getConversation(id, user));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<PaginatedResponse<MessageSummary>> getMessages(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "") String q,
            @PageableDefault(size = 50) Pageable pageable) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(chatService.getConversationMessages(id, user, q, pageable));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody ChatDtos.CreateMessageRequest request) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(user, id, request));
    }

    @PostMapping("/conversations/{id}/read")
    public ResponseEntity<AuthDtos.MessageResponse> markRead(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = userContext.currentUser(principal);
        chatService.markRead(user, id);
        return ResponseEntity.ok(new AuthDtos.MessageResponse("Conversation marked as read"));
    }
}
