package com.example.ecommerce.marketplace.web.model.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for marking notifications as read.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarkAsReadRequest {
    /**
     * If true, marks all notifications as read. If false, marks only the notification
     * specified by the path variable.
     */
    private Boolean markAll;
}

