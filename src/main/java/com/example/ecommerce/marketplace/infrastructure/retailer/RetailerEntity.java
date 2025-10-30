package com.example.ecommerce.marketplace.infrastructure.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Retailer.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "retailers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetailerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    private String profilePicture;

    @Column(length = 1000)
    private String profileDescription;

    @Column(nullable = false, unique = true)
    private String businessLicense;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetailerLoyaltyTier loyaltyTier;

    private Double creditLimit;

    private Double totalPurchaseAmount;

    private Integer loyaltyPoints;

    @Column(nullable = false)
    private String accountStatus;

    /**
     * Converts JPA entity to domain model.
     */
    public Retailer toDomain() {
        Retailer retailer = new Retailer();
        retailer.setId(this.id);
        retailer.setName(this.name);
        retailer.setEmail(this.email);
        retailer.setPhone(this.phone);
        retailer.setAddress(this.address);
        retailer.setProfilePicture(this.profilePicture);
        retailer.setProfileDescription(this.profileDescription);
        retailer.setBusinessLicense(this.businessLicense);
        retailer.setLoyaltyTier(this.loyaltyTier);
        retailer.setCreditLimit(this.creditLimit);
        retailer.setTotalPurchaseAmount(this.totalPurchaseAmount);
        retailer.setLoyaltyPoints(this.loyaltyPoints);
        return retailer;
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static RetailerEntity fromDomain(Retailer retailer) {
        RetailerEntity entity = new RetailerEntity();
        entity.setId(retailer.getId());
        entity.setName(retailer.getName());
        entity.setEmail(retailer.getEmail());
        entity.setPhone(retailer.getPhone());
        entity.setAddress(retailer.getAddress());
        entity.setProfilePicture(retailer.getProfilePicture());
        entity.setProfileDescription(retailer.getProfileDescription());
        entity.setBusinessLicense(retailer.getBusinessLicense());
        entity.setLoyaltyTier(retailer.getLoyaltyTier());
        entity.setCreditLimit(retailer.getCreditLimit());
        entity.setTotalPurchaseAmount(retailer.getTotalPurchaseAmount());
        entity.setLoyaltyPoints(retailer.getLoyaltyPoints());

        // Set default account status if null
        entity.setAccountStatus("ACTIVE");

        return entity;
    }
}
