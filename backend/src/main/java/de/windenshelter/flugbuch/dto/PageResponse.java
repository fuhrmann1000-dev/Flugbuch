package de.windenshelter.flugbuch.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Simplified, stable public shape for a paginated response. Used instead of
 * returning Spring Data's {@link Page} directly from a controller, which
 * serializes a lot of internal/redundant structure (a nested "pageable"
 * object duplicating page/size/sort, "empty", "numberOfElements", etc).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
