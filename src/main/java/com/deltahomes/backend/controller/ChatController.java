package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.communication.Conversation;
import com.deltahomes.backend.entity.communication.Message;
import com.deltahomes.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getConversationMessages(id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long id,
            @RequestBody Message message) {
        // Stub: requires user authentication
        return ResponseEntity.ok(message);
    }
}
