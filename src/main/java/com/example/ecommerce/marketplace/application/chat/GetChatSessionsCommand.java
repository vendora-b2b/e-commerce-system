package com.example.ecommerce.marketplace.application.chat;

/**
 * Command object for retrieving chat sessions for a user.
 */
public class GetChatSessionsCommand {

    private final Long userId;
    private final Boolean activeOnly;

    public GetChatSessionsCommand(Long userId, Boolean activeOnly) {
        this.userId = userId;
        this.activeOnly = activeOnly;
    }

    /**
     * Creates a command to get all sessions for a user.
     *
     * @param userId the user ID
     * @return a new command
     */
    public static GetChatSessionsCommand all(Long userId) {
        return new GetChatSessionsCommand(userId, false);
    }

    /**
     * Creates a command to get only active sessions for a user.
     *
     * @param userId the user ID
     * @return a new command
     */
    public static GetChatSessionsCommand activeOnly(Long userId) {
        return new GetChatSessionsCommand(userId, true);
    }

    public Long getUserId() {
        return userId;
    }

    public Boolean getActiveOnly() {
        return activeOnly != null ? activeOnly : false;
    }
}
