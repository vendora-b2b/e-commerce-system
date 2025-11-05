package com.example.ecommerce.marketplace.application.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case for updating an existing retailer's profile information.
 * Framework-agnostic, following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class UpdateRetailerProfileUseCase {

    private final RetailerRepository retailerRepository;

    /**
     * Executes the retailer profile update use case.
     *
     * @param command the update command containing retailer ID and new profile data
     * @return the result indicating success or failure with details
     */
    public UpdateRetailerProfileResult execute(UpdateRetailerProfileCommand command) {
        // 1. Validate retailer ID
        if (command.getRetailerId() == null) {
            return UpdateRetailerProfileResult.failure("Retailer ID is required", "INVALID_RETAILER_ID");
        }

        // 2. Find retailer
        Optional<Retailer> retailerOpt = retailerRepository.findById(command.getRetailerId());
        if (retailerOpt.isEmpty()) {
            return UpdateRetailerProfileResult.failure("Retailer not found", "RETAILER_NOT_FOUND");
        }

        Retailer retailer = retailerOpt.get();

        // 3. Update profile using domain logic
        retailer.updateProfile(
            command.getName(),
            command.getPhone(),
            command.getAddress(),
            command.getProfileDescription()
        );

        // 4. Save updated retailer
        Retailer updatedRetailer = retailerRepository.save(retailer);

        // 5. Return success result
        return UpdateRetailerProfileResult.success(updatedRetailer.getId());
    }
}
