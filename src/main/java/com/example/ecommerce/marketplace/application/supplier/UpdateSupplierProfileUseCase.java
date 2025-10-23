package com.example.ecommerce.marketplace.application.supplier;

import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Use case for updating an existing supplier's profile information.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
public class UpdateSupplierProfileUseCase {

    private final SupplierRepository supplierRepository;

    /**
     * Executes the supplier profile update use case.
     *
     * @param command the update command containing supplier ID and new profile data
     * @return the result indicating success or failure with details
     */
    public UpdateSupplierProfileResult execute(UpdateSupplierProfileCommand command) {
        // 1. Validate supplier ID
        if (command.getSupplierId() == null) {
            return UpdateSupplierProfileResult.failure("Supplier ID is required", "INVALID_SUPPLIER_ID");
        }

        // 2. Find supplier
        Optional<Supplier> supplierOpt = supplierRepository.findById(command.getSupplierId());
        if (supplierOpt.isEmpty()) {
            return UpdateSupplierProfileResult.failure("Supplier not found", "SUPPLIER_NOT_FOUND");
        }

        Supplier supplier = supplierOpt.get();

        // 3. Update profile using domain logic
        supplier.updateProfile(
            command.getName(),
            command.getPhone(),
            command.getAddress(),
            command.getProfileDescription()
        );

        // 4. Save updated supplier
        Supplier updatedSupplier = supplierRepository.save(supplier);

        // 5. Return success result
        return UpdateSupplierProfileResult.success(updatedSupplier.getId());
    }
}
