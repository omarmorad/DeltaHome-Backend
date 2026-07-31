package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.ChatService;
import com.deltahomes.backend.service.UserContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ChatController {

    private final ChatService chatService;
    private final UserContext userContext;

    public ChatController(ChatService chatService, UserContext userContext) {
        this.chatService = chatService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ConversationSummary>> index(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(chatService.indexConversations(user, pageable));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<PaginatedResponse<MessageSummary>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "") String q,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(chatService.getConversationMessages(id, q, pageable));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID id,
            @RequestBody Message message) {
        // Stub: requires user authentication
        return ResponseEntity.ok(message);
    }
}
