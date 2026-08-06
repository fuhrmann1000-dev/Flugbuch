/** Mirrors the backend's PageResponse<T> - how every paginated list endpoint wraps its results. */
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}
