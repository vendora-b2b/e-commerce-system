package com.example.ecommerce.marketplace.infrastructure.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Supplier.
 * This is the persistence model, separate from the domain model.
 */
@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierEntity {

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

    private Double rating;

    @Column(nullable = false)
    private Boolean verified;

    /**
     * Converts JPA entity to domain model.
     */
    public Supplier toDomain() {
        return new Supplier(
            this.id,
            this.name,
            this.email,
            this.phone,
            this.address,
            this.profilePicture,
            this.profileDescription,
            this.businessLicense,
            this.rating,
            this.verified
        );
    }

    /**
     * Creates JPA entity from domain model.
     */
    public static SupplierEntity fromDomain(Supplier supplier) {
        return new SupplierEntity(
            supplier.getId(),
            supplier.getName(),
            supplier.getEmail(),
            supplier.getPhone(),
            supplier.getAddress(),
            supplier.getProfilePicture(),
            supplier.getProfileDescription(),
            supplier.getBusinessLicense(),
            supplier.getRating(),
            supplier.getVerified()
        );
    }
}
