package com.example.ecommerce.marketplace.web.model.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP response DTO for supplier information.
 * Represents a supplier entity in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
    private String profileDescription;
    private String businessLicense;
    private Double rating;
    private Boolean verified;

    /**
     * Creates a SupplierResponse from a domain Supplier entity.
     */
    public static SupplierResponse fromDomain(Supplier supplier) {
        return new SupplierResponse(
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
