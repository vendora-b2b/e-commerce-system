package com.example.ecommerce.marketplace.infrastructure.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for UserInteractionEntity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface JpaUserInteractionRepository extends JpaRepository<UserInteractionEntity, Long> {

    List<UserInteractionEntity> findByUserId(Long userId);

    List<UserInteractionEntity> findByProductId(Long productId);

    List<UserInteractionEntity> findByUserIdAndProductId(Long userId, Long productId);

    List<UserInteractionEntity> findByUserIdAndInteractionType(Long userId, InteractionType interactionType);

    List<UserInteractionEntity> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    List<UserInteractionEntity> findByUserIdAndInteractionTypeIn(Long userId, List<InteractionType> types);

    @Query("SELECT i FROM UserInteractionEntity i WHERE i.userId = :userId ORDER BY i.createdAt DESC LIMIT :limit")
    List<UserInteractionEntity> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    long countByUserId(Long userId);

    long countByProductId(Long productId);

    long countByUserIdAndInteractionType(Long userId, InteractionType interactionType);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductIdAndInteractionType(Long userId, Long productId, InteractionType interactionType);

    @Modifying
    @Query("DELETE FROM UserInteractionEntity i WHERE i.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") LocalDateTime before);

    @Query("SELECT DISTINCT i.productId FROM UserInteractionEntity i WHERE i.userId = :userId")
    List<Long> findDistinctProductIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT i.userId FROM UserInteractionEntity i WHERE i.productId = :productId")
    List<Long> findDistinctUserIdsByProductId(@Param("productId") Long productId);

    @Query("SELECT i.productId, COUNT(i) as cnt FROM UserInteractionEntity i " +
           "WHERE i.userId = :userId GROUP BY i.productId ORDER BY cnt DESC LIMIT :limit")
    List<Object[]> findTopProductsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query("SELECT i.interactionType, COUNT(i) FROM UserInteractionEntity i " +
           "WHERE i.productId = :productId GROUP BY i.interactionType")
    List<Object[]> countByProductIdGroupByType(@Param("productId") Long productId);
}
