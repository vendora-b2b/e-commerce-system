package com.example.ecommerce.marketplace.application.ai;

/**
 * Command object for deleting a supplier from the AI vector database.
 */
public class DeleteSupplierFromAiCommand {

    private final Long supplierId;

    public DeleteSupplierFromAiCommand(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() {
        return supplierId;
    }
}
