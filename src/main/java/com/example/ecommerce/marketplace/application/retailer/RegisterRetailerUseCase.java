package com.example.ecommerce.marketplace.application.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case for registering a new retailer in the marketplace.
 * Handles validation, uniqueness checks, and initial retailer setup.
 * Framework-agnostic, following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class RegisterRetailerUseCase {

    private final RetailerRepository retailerRepository;

    /**
     * Executes the retailer registration use case.
     *
     * @param command the registration command containing retailer data
     * @return the result indicating success or failure with details
     */
    public RegisterRetailerResult execute(RegisterRetailerCommand command) {
        // 1. Validate required fields
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            return RegisterRetailerResult.failure("Retailer name is required", "INVALID_NAME");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            return RegisterRetailerResult.failure("Email is required", "INVALID_EMAIL");
        }
        if (command.getBusinessLicense() == null || command.getBusinessLicense().trim().isEmpty()) {
            return RegisterRetailerResult.failure("Business license is required", "INVALID_LICENSE");
        }

        // 2. Create retailer domain object
        Retailer retailer = new Retailer();
        retailer.setName(command.getName());
        retailer.setEmail(command.getEmail());
        retailer.setPhone(command.getPhone());
        retailer.setAddress(command.getAddress());
        retailer.setProfilePicture(command.getProfilePicture());
        retailer.setProfileDescription(command.getProfileDescription());
        retailer.setBusinessLicense(command.getBusinessLicense());
        retailer.setLoyaltyTier(RetailerLoyaltyTier.BRONZE); // Initial tier
        retailer.setCreditLimit(command.getCreditLimit() != null ? command.getCreditLimit() : 0.0);
        retailer.setTotalPurchaseAmount(0.0);
        retailer.setLoyaltyPoints(0);

        // 3. Validate email format using domain logic
        if (!retailer.validateEmail()) {
            return RegisterRetailerResult.failure("Invalid email format", "INVALID_EMAIL_FORMAT");
        }

        // 4. Validate business license format using domain logic
        if (!retailer.validateBusinessLicense()) {
            return RegisterRetailerResult.failure(
                "Invalid business license format (8-20 uppercase alphanumeric characters)",
                "INVALID_LICENSE_FORMAT"
            );
        }

        // 5. Check email uniqueness
        if (retailerRepository.existsByEmail(command.getEmail())) {
            return RegisterRetailerResult.failure("Email already registered", "EMAIL_EXISTS");
        }

        // 6. Check business license uniqueness
        if (retailerRepository.existsByBusinessLicense(command.getBusinessLicense())) {
            return RegisterRetailerResult.failure("Business license already registered", "LICENSE_EXISTS");
        }

        // 7. Save retailer
        Retailer savedRetailer = retailerRepository.save(retailer);

        // 8. Return success result
        return RegisterRetailerResult.success(savedRetailer.getId());
    }
}
