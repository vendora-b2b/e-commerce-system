package com.example.ecommerce.marketplace.web.model.retailer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HTTP request DTO for retailer profile update.
 * All fields are optional - only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRetailerRequest {

    private String name;
    private String phone;
    private String address;
    private String profileDescription;
}
