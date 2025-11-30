package com.example.ecommerce.marketplace.web.model.chat;

import com.example.ecommerce.marketplace.domain.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * HTTP response DTO for ask question results.
 * Contains the AI-generated response and any source references.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AskQuestionResponse {

    private ChatMessageResponse userMessage;
    private ChatMessageResponse assistantMessage;
    private List<Map<String, Object>> sources;
    private String queryType;

    /**
     * Creates an AskQuestionResponse from domain entities.
     *
     * @param userMessage      the user's message
     * @param assistantMessage the AI assistant's response
     * @param sources          source references used in the response
     * @param queryType        the classified query type
     * @return the response DTO
     */
    public static AskQuestionResponse from(ChatMessage userMessage, ChatMessage assistantMessage,
                                            List<Map<String, Object>> sources, String queryType) {
        return new AskQuestionResponse(
            ChatMessageResponse.fromDomain(userMessage),
            ChatMessageResponse.fromDomain(assistantMessage),
            sources,
            queryType
        );
    }
}
