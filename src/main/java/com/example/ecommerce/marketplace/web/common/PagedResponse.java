package com.example.ecommerce.marketplace.web.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Provides a consistent structure for paginated API responses.
 *
 * @param <T> the type of content in the page
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private PageInfo page;

    /**
     * Page metadata information.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;
    }

    /**
     * Creates a paged response from Spring Data Page.
     */
    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        PageInfo pageInfo = new PageInfo(
            page.getSize(),
            page.getNumber(),
            page.getTotalElements(),
            page.getTotalPages()
        );
        return new PagedResponse<>(page.getContent(), pageInfo);
    }
}
