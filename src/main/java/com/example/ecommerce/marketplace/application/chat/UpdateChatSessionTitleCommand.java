package com.example.ecommerce.marketplace.application.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command for updating a chat session title.
 */
@Getter
@AllArgsConstructor
public class UpdateChatSessionTitleCommand {
    private final Long sessionId;
    private final Long userId;
    private final String newTitle;
}
