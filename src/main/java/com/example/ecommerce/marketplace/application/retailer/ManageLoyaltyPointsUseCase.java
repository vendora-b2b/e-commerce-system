package com.example.ecommerce.marketplace.application.retailer;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case for managing retailer loyalty points.
 * Handles both adding and redeeming points with tier updates.
 * Framework-agnostic, following Clean Architecture principles.
 */
@Service
@RequiredArgsConstructor
public class ManageLoyaltyPointsUseCase {

    private final RetailerRepository retailerRepository;

    /**
     * Executes the loyalty points management use case.
     *
     * @param command the command containing retailer ID, points, and operation type
     * @return the result indicating success or failure with updated points and tier
     */
    public ManageLoyaltyPointsResult execute(ManageLoyaltyPointsCommand command) {
        // 1. Validate retailer ID
        if (command.getRetailerId() == null) {
            return ManageLoyaltyPointsResult.failure("Retailer ID is required", "INVALID_RETAILER_ID");
        }

        // 2. Validate points
        if (command.getPoints() == null || command.getPoints() <= 0) {
            return ManageLoyaltyPointsResult.failure("Points must be positive", "INVALID_POINTS");
        }

        // 3. Validate operation type
        if (command.getOperationType() == null) {
            return ManageLoyaltyPointsResult.failure("Operation type is required", "INVALID_OPERATION");
        }

        // 4. Find retailer
        Optional<Retailer> retailerOpt = retailerRepository.findById(command.getRetailerId());
        if (retailerOpt.isEmpty()) {
            return ManageLoyaltyPointsResult.failure("Retailer not found", "RETAILER_NOT_FOUND");
        }

        Retailer retailer = retailerOpt.get();

        // 5. Execute operation based on type
        String message;
        try {
            if (command.getOperationType() == ManageLoyaltyPointsCommand.OperationType.ADD) {
                retailer.addLoyaltyPoints(command.getPoints());
                message = "Successfully added " + command.getPoints() + " loyalty points";
            } else {
                boolean redeemed = retailer.redeemLoyaltyPoints(command.getPoints());
                if (!redeemed) {
                    return ManageLoyaltyPointsResult.failure(
                        "Insufficient loyalty points for redemption",
                        "INSUFFICIENT_POINTS"
                    );
                }
                message = "Successfully redeemed " + command.getPoints() + " loyalty points";
            }
        } catch (IllegalArgumentException e) {
            return ManageLoyaltyPointsResult.failure(e.getMessage(), "INVALID_OPERATION");
        }

        // 6. Save updated retailer
        Retailer updatedRetailer = retailerRepository.save(retailer);

        // 7. Return success result with new balance and tier
        return ManageLoyaltyPointsResult.success(
            updatedRetailer.getId(),
            updatedRetailer.getLoyaltyPoints(),
            updatedRetailer.getLoyaltyTier(),
            message
        );
    }
}
