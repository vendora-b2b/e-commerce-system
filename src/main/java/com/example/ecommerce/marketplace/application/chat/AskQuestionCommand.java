package com.example.ecommerce.marketplace.application.chat;

/**
 * Command object for asking a question in a chat session.
 * Contains the user's query and context for AI response generation.
 */
public class AskQuestionCommand {

    private final Long sessionId;
    private final Long userId;
    private final String question;
    private final String userType;  // "retailer" or "supplier"
    private final String userName;
    private final String loyaltyTier;  // For retailers

    private AskQuestionCommand(Builder builder) {
        this.sessionId = builder.sessionId;
        this.userId = builder.userId;
        this.question = builder.question;
        this.userType = builder.userType;
        this.userName = builder.userName;
        this.loyaltyTier = builder.loyaltyTier;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getQuestion() {
        return question;
    }

    public String getUserType() {
        return userType;
    }

    public String getUserName() {
        return userName;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long sessionId;
        private Long userId;
        private String question;
        private String userType;
        private String userName;
        private String loyaltyTier;

        public Builder sessionId(Long sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder userType(String userType) {
            this.userType = userType;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder loyaltyTier(String loyaltyTier) {
            this.loyaltyTier = loyaltyTier;
            return this;
        }

        public AskQuestionCommand build() {
            return new AskQuestionCommand(this);
        }
    }
}
