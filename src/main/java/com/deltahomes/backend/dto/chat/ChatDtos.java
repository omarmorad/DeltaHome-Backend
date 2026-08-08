package com.deltahomes.backend.dto.chat;

import com.deltahomes.backend.entity.enums.MessageType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request/response payloads for the chat module.
 */
public final class ChatDtos {

    private ChatDtos() {
    }

    public record CreateConversationRequest(
            @NotEmpty(message = "participantIds is required")
            List<UUID> participantIds
    ) {
    }

    public record CreateMessageRequest(
            MessageType type,

            @Size(max = 10_000, message = "Message must be at most 10000 characters")
            String content,

            @Size(max = 255, message = "Media URL must be at most 255 characters")
            String mediaUrl,

            @Size(max = 10_000, message = "Payload must be at most 10000 characters")
            String payload
    ) {
    }
}
