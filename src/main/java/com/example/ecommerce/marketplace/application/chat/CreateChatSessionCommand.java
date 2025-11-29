package com.example.ecommerce.marketplace.application.chat;

/**
 * Command object for creating a new chat session.
 * Contains all necessary data for session creation.
 */
public class CreateChatSessionCommand {

    private final Long userId;
    private final String title;

    public CreateChatSessionCommand(Long userId, String title) {
        this.userId = userId;
        this.title = title;
    }

    /**
     * Creates a command for a new chat session with default title.
     *
     * @param userId the user ID
     * @return a new command
     */
    public static CreateChatSessionCommand withDefaults(Long userId) {
        return new CreateChatSessionCommand(userId, null);
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }
}
