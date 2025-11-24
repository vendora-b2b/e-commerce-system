package com.example.ecommerce.marketplace.web.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Supplier information included in login response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInfo {
    private Long id;
    private String name;
    private String email;
    private String businessLicense;
    private Double rating;
    private Boolean verified;
}
