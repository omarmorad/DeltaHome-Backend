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

    // ---------- Responses ----------

    /**
     * Conversation projection. {@code otherUser} is resolved relative to the
     * authenticated viewer (never the viewer themselves).
     */
    public record ConversationResponse(
            UUID id,
            String lastMessagePreview,
            java.time.OffsetDateTime updatedAt,
            OtherUserInfo otherUser,
            UUID lastSeenByViewerId,
            boolean unreadForViewer
    ) {

        public record OtherUserInfo(UUID id, String name, String photoUrl) {
        }
    }

    /** Message projection — never exposes the raw entity. */
    public record MessageResponse(
            UUID id,
            UUID conversationId,
            UUID senderId,
            String senderName,
            MessageType type,
            String textBody,
            String mediaUrl,
            String payload,
            java.time.OffsetDateTime createdAt
    ) {
    }
}
