package com.example.ecommerce.marketplace.application.notification;

import com.example.ecommerce.marketplace.domain.notification.Notification;
import com.example.ecommerce.marketplace.domain.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service facade for notification operations.
 * Provides convenient methods for creating notifications in other use cases.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final CreateNotificationUseCase createNotificationUseCase;

    /**
     * Creates a notification synchronously.
     */
    public Notification createNotification(Long userId, NotificationType type,
                                           String title, String message,
                                           Long referenceId, String referenceType) {
        CreateNotificationCommand command = new CreateNotificationCommand(
            userId, type, title, message, referenceId, referenceType
        );
        CreateNotificationResult result = createNotificationUseCase.execute(command);
        return result.isSuccess() ? result.getNotification() : null;
    }

    /**
     * Creates a notification asynchronously (fire and forget).
     */
    @Async
    public void createNotificationAsync(Long userId, NotificationType type,
                                        String title, String message,
                                        Long referenceId, String referenceType) {
        try {
            createNotification(userId, type, title, message, referenceId, referenceType);
            log.debug("Notification created for user {} - type: {}", userId, type);
        } catch (Exception e) {
            log.error("Failed to create notification for user {} - type: {}: {}",
                userId, type, e.getMessage());
        }
    }

    /**
     * Notifies supplier about a new order.
     */
    public void notifyOrderPlaced(Long supplierUserId, Long orderId, String orderNumber, 
                                  String retailerName, Double totalAmount) {
        log.info("Creating ORDER_PLACED notification for supplier user {} - order {}", supplierUserId, orderNumber);
        String title = "New Order Received";
        String message = String.format(
            "New order #%s from %s for $%.2f has been placed.",
            orderNumber, retailerName, totalAmount
        );
        try {
            createNotification(supplierUserId, NotificationType.ORDER_PLACED, 
                title, message, orderId, "ORDER");
            log.info("Successfully created ORDER_PLACED notification for supplier user {}", supplierUserId);
        } catch (Exception e) {
            log.error("Failed to create ORDER_PLACED notification for supplier: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifies retailer about their new order placement.
     */
    public void notifyRetailerOrderPlaced(Long retailerUserId, Long orderId, String orderNumber,
                                          String supplierName, Double totalAmount) {
        log.info("Creating ORDER_PLACED notification for retailer user {} - order {}", retailerUserId, orderNumber);
        String title = "Order Placed Successfully";
        String message = String.format(
            "Your order #%s to %s for $%.2f has been placed successfully.",
            orderNumber, supplierName, totalAmount
        );
        try {
            createNotification(retailerUserId, NotificationType.ORDER_PLACED,
                title, message, orderId, "ORDER");
            log.info("Successfully created ORDER_PLACED notification for retailer user {}", retailerUserId);
        } catch (Exception e) {
            log.error("Failed to create ORDER_PLACED notification for retailer: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifies retailer that their order was confirmed.
     */
    @Async
    public void notifyOrderConfirmed(Long retailerUserId, Long orderId, String orderNumber,
                                     String supplierName) {
        String title = "Order Confirmed";
        String message = String.format(
            "Your order #%s has been confirmed by %s.",
            orderNumber, supplierName
        );
        createNotificationAsync(retailerUserId, NotificationType.ORDER_CONFIRMED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies supplier that an order was confirmed.
     */
    @Async
    public void notifySupplierOrderConfirmed(Long supplierUserId, Long orderId, String orderNumber,
                                             String retailerName) {
        String title = "Order Confirmed";
        String message = String.format(
            "Order #%s from %s has been confirmed.",
            orderNumber, retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.ORDER_CONFIRMED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies retailer that their order was shipped.
     */
    @Async
    public void notifyOrderShipped(Long retailerUserId, Long orderId, String orderNumber,
                                   String supplierName) {
        String title = "Order Shipped";
        String message = String.format(
            "Your order #%s has been shipped by %s.",
            orderNumber, supplierName
        );
        createNotificationAsync(retailerUserId, NotificationType.ORDER_SHIPPED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies supplier that an order was shipped.
     */
    @Async
    public void notifySupplierOrderShipped(Long supplierUserId, Long orderId, String orderNumber,
                                           String retailerName) {
        String title = "Order Shipped";
        String message = String.format(
            "Order #%s to %s has been shipped.",
            orderNumber, retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.ORDER_SHIPPED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies retailer that their order was delivered.
     */
    @Async
    public void notifyOrderDelivered(Long retailerUserId, Long orderId, String orderNumber) {
        String title = "Order Delivered";
        String message = String.format(
            "Your order #%s has been delivered.",
            orderNumber
        );
        createNotificationAsync(retailerUserId, NotificationType.ORDER_DELIVERED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies supplier that an order was delivered.
     */
    @Async
    public void notifySupplierOrderDelivered(Long supplierUserId, Long orderId, String orderNumber,
                                             String retailerName) {
        String title = "Order Delivered";
        String message = String.format(
            "Order #%s to %s has been delivered.",
            orderNumber, retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.ORDER_DELIVERED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies about order cancellation.
     */
    @Async
    public void notifyOrderCancelled(Long userId, Long orderId, String orderNumber,
                                     String cancelledBy) {
        String title = "Order Cancelled";
        String message = String.format(
            "Order #%s has been cancelled by %s.",
            orderNumber, cancelledBy
        );
        createNotificationAsync(userId, NotificationType.ORDER_CANCELLED,
            title, message, orderId, "ORDER");
    }

    /**
     * Notifies supplier about a new quotation request.
     */
    @Async
    public void notifyQuotationRequested(Long supplierUserId, Long quotationId,
                                         String retailerName) {
        String title = "New Quotation Request";
        String message = String.format(
            "You have received a new quotation request from %s.",
            retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.QUOTATION_REQUESTED,
            title, message, quotationId, "QUOTATION");
    }

    /**
     * Notifies retailer about quotation response.
     */
    @Async
    public void notifyQuotationResponded(Long retailerUserId, Long quotationId,
                                         String supplierName) {
        String title = "Quotation Response Received";
        String message = String.format(
            "%s has responded to your quotation request.",
            supplierName
        );
        createNotificationAsync(retailerUserId, NotificationType.QUOTATION_RESPONDED,
            title, message, quotationId, "QUOTATION");
    }

    /**
     * Notifies supplier that quotation was accepted.
     */
    @Async
    public void notifyQuotationAccepted(Long supplierUserId, Long quotationId,
                                        String retailerName) {
        String title = "Quotation Accepted";
        String message = String.format(
            "%s has accepted your quotation.",
            retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.QUOTATION_ACCEPTED,
            title, message, quotationId, "QUOTATION");
    }

    /**
     * Notifies supplier that quotation was rejected.
     */
    @Async
    public void notifyQuotationRejected(Long supplierUserId, Long quotationId,
                                        String retailerName) {
        String title = "Quotation Rejected";
        String message = String.format(
            "%s has rejected your quotation.",
            retailerName
        );
        createNotificationAsync(supplierUserId, NotificationType.QUOTATION_REJECTED,
            title, message, quotationId, "QUOTATION");
    }

    /**
     * Notifies about quotation finalization.
     */
    @Async
    public void notifyQuotationFinalized(Long userId, Long quotationId, boolean orderCreated) {
        String title = "Quotation Finalized";
        String message = orderCreated
            ? "The quotation has been finalized and a purchase order has been created."
            : "The quotation has been finalized.";
        createNotificationAsync(userId, NotificationType.QUOTATION_FINALIZED,
            title, message, quotationId, "QUOTATION");
    }
}

