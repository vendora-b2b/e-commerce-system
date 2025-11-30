package com.example.ecommerce.marketplace.infrastructure.analytics;

import com.example.ecommerce.marketplace.domain.analytics.InteractionType;
import com.example.ecommerce.marketplace.domain.analytics.UserInteraction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity for UserInteraction.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "user_interactions",
    indexes = {
        @Index(name = "idx_interaction_user", columnList = "user_id"),
        @Index(name = "idx_interaction_product", columnList = "product_id"),
        @Index(name = "idx_interaction_type", columnList = "interaction_type"),
        @Index(name = "idx_interaction_created", columnList = "created_at"),
        @Index(name = "idx_interaction_user_product", columnList = "user_id, product_id"),
        @Index(name = "idx_interaction_user_type", columnList = "user_id, interaction_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 30)
    private InteractionType interactionType;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @ElementCollection
    @CollectionTable(
        name = "user_interaction_metadata",
        joinColumns = @JoinColumn(name = "interaction_id")
    )
    @MapKeyColumn(name = "meta_key", length = 50)
    @Column(name = "meta_value", length = 255)
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Converts JPA entity to domain model.
     */
    public UserInteraction toDomain() {
        return new UserInteraction(
            this.id,
            this.userId,
            this.productId,
            this.variantId,
            this.interactionType,
            this.sessionId,
            this.metadata != null ? new HashMap<>(this.metadata) : null,
            this.createdAt
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static UserInteractionEntity fromDomain(UserInteraction interaction) {
        UserInteractionEntity entity = new UserInteractionEntity();
        entity.setId(interaction.getId());
        entity.setUserId(interaction.getUserId());
        entity.setProductId(interaction.getProductId());
        entity.setVariantId(interaction.getVariantId());
        entity.setInteractionType(interaction.getInteractionType());
        entity.setSessionId(interaction.getSessionId());
        entity.setMetadata(interaction.getMetadata() != null ? new HashMap<>(interaction.getMetadata()) : new HashMap<>());
        entity.setCreatedAt(interaction.getCreatedAt() != null ? interaction.getCreatedAt() : LocalDateTime.now());
        return entity;
    }
}
