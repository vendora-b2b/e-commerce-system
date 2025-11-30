package com.example.ecommerce.marketplace.domain.analytics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserInteraction entity.
 * Defines persistence operations for user interactions following the repository pattern.
 */
public interface UserInteractionRepository {

    /**
     * Saves a new user interaction.
     *
     * @param interaction the interaction to save
     * @return the saved interaction with generated ID
     */
    UserInteraction save(UserInteraction interaction);

    /**
     * Saves multiple user interactions.
     *
     * @param interactions the list of interactions to save
     * @return the saved interactions
     */
    List<UserInteraction> saveAll(List<UserInteraction> interactions);

    /**
     * Finds an interaction by its unique identifier.
     *
     * @param id the interaction ID
     * @return an Optional containing the interaction if found
     */
    Optional<UserInteraction> findById(Long id);

    /**
     * Finds all interactions by a user.
     *
     * @param userId the user ID
     * @return list of interactions for the user
     */
    List<UserInteraction> findByUserId(Long userId);

    /**
     * Finds all interactions for a product.
     *
     * @param productId the product ID
     * @return list of interactions for the product
     */
    List<UserInteraction> findByProductId(Long productId);

    /**
     * Finds all interactions by a user for a specific product.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @return list of interactions
     */
    List<UserInteraction> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Finds interactions by user and type.
     *
     * @param userId          the user ID
     * @param interactionType the interaction type
     * @return list of interactions
     */
    List<UserInteraction> findByUserIdAndInteractionType(Long userId, InteractionType interactionType);

    /**
     * Finds recent interactions by a user.
     *
     * @param userId the user ID
     * @param since  the timestamp to search from
     * @return list of recent interactions
     */
    List<UserInteraction> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    /**
     * Finds the most recent interactions by a user.
     *
     * @param userId the user ID
     * @param limit  maximum number of interactions to return
     * @return list of recent interactions
     */
    List<UserInteraction> findRecentByUserId(Long userId, int limit);

    /**
     * Finds high-value interactions (purchase, add-to-cart) by user.
     *
     * @param userId the user ID
     * @param types  list of interaction types to include
     * @return list of high-value interactions
     */
    List<UserInteraction> findByUserIdAndInteractionTypeIn(Long userId, List<InteractionType> types);

    /**
     * Counts interactions by user.
     *
     * @param userId the user ID
     * @return count of interactions
     */
    long countByUserId(Long userId);

    /**
     * Counts interactions by product.
     *
     * @param productId the product ID
     * @return count of interactions
     */
    long countByProductId(Long productId);

    /**
     * Counts interactions by type for a user.
     *
     * @param userId          the user ID
     * @param interactionType the interaction type
     * @return count of interactions
     */
    long countByUserIdAndInteractionType(Long userId, InteractionType interactionType);

    /**
     * Checks if a user has interacted with a product.
     *
     * @param userId    the user ID
     * @param productId the product ID
     * @return true if the user has any interaction with the product
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Checks if a user has a specific type of interaction with a product.
     *
     * @param userId          the user ID
     * @param productId       the product ID
     * @param interactionType the interaction type
     * @return true if such interaction exists
     */
    boolean existsByUserIdAndProductIdAndInteractionType(Long userId, Long productId, InteractionType interactionType);

    /**
     * Deletes old interactions before a certain date.
     *
     * @param before the cutoff date
     * @return number of deleted interactions
     */
    long deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * Finds distinct product IDs that a user has interacted with.
     *
     * @param userId the user ID
     * @return list of product IDs
     */
    List<Long> findDistinctProductIdsByUserId(Long userId);

    /**
     * Finds users who interacted with a specific product.
     *
     * @param productId the product ID
     * @return list of user IDs
     */
    List<Long> findDistinctUserIdsByProductId(Long productId);
}
