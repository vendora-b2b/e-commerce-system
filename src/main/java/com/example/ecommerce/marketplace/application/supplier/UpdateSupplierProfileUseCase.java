package com.example.ecommerce.marketplace.application.supplier;

import com.example.ecommerce.marketplace.application.ai.IngestSupplierCommand;
import com.example.ecommerce.marketplace.application.ai.IngestSupplierUseCase;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Use case for updating an existing supplier's profile information.
 * Framework-agnostic, following Clean Architecture principles.
 */
@RequiredArgsConstructor
@Slf4j
public class UpdateSupplierProfileUseCase {

    private final SupplierRepository supplierRepository;
    private final IngestSupplierUseCase ingestSupplierUseCase;

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

        // 5. Ingest supplier to AI service for search (async - fire and forget)
        ingestSupplierToAi(updatedSupplier);

        // 6. Return success result
        return UpdateSupplierProfileResult.success(updatedSupplier.getId());
    }

    /**
     * Asynchronously ingest the supplier to the AI service for vector search.
     * This is fire-and-forget - failures are logged but don't affect the main flow.
     */
    private void ingestSupplierToAi(Supplier supplier) {
        try {
            IngestSupplierCommand ingestCommand = IngestSupplierCommand.builder()
                    .supplierId(supplier.getId())
                    .name(supplier.getName())
                    .email(supplier.getEmail())
                    .phone(supplier.getPhone())
                    .address(supplier.getAddress())
                    .businessLicense(supplier.getBusinessLicense())
                    .build();

            ingestSupplierUseCase.executeAsync(ingestCommand);
            log.debug("Triggered async supplier ingestion for supplier ID: {}", supplier.getId());
        } catch (Exception e) {
            log.warn("Failed to trigger supplier ingestion for supplier ID: {}. Error: {}", 
                    supplier.getId(), e.getMessage());
            // Don't fail the main operation if AI ingestion fails
        }
    }
}
