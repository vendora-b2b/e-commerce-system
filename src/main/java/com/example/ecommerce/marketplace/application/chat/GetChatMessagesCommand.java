package com.example.ecommerce.marketplace.application.chat;

/**
 * Command object for retrieving chat messages from a session.
 */
public class GetChatMessagesCommand {

    private final Long sessionId;
    private final Long userId;  // For authorization check
    private final Integer limit;
    private final Long beforeMessageId;  // For pagination

    public GetChatMessagesCommand(Long sessionId, Long userId, Integer limit, Long beforeMessageId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.limit = limit;
        this.beforeMessageId = beforeMessageId;
    }

    /**
     * Creates a command to get all messages in a session.
     *
     * @param sessionId the session ID
     * @param userId    the user ID for authorization
     * @return a new command
     */
    public static GetChatMessagesCommand all(Long sessionId, Long userId) {
        return new GetChatMessagesCommand(sessionId, userId, null, null);
    }

    /**
     * Creates a command to get recent messages with a limit.
     *
     * @param sessionId the session ID
     * @param userId    the user ID for authorization
     * @param limit     maximum number of messages to return
     * @return a new command
     */
    public static GetChatMessagesCommand recent(Long sessionId, Long userId, int limit) {
        return new GetChatMessagesCommand(sessionId, userId, limit, null);
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getLimit() {
        return limit;
    }

    public Long getBeforeMessageId() {
        return beforeMessageId;
    }
}
