/** Response from POST /imports/main-flight-log - matches the backend's ImportResultDto. */
export interface ImportResultDto {
    imported: number;
    skipped: number;
}
