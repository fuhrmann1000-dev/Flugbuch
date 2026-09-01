import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { ImportResultDto } from '../models/import.model';

/**
 * Talks to /api/v1/imports - the manual CSV upload on the Data Management
 * page, replacing the legacy "daten_importieren.php" page. Requires a
 * logged-in pilot (no particular role); authInterceptor attaches the JWT
 * like any other authenticated call.
 */
@Injectable({
    providedIn: 'root',
})
export class ImportService {
    private readonly httpClient = inject(HttpClient);
    private readonly apiBaseUrl = `${API_BASE_URL}/imports`;

    /** Uploads the CSV and returns how many rows were stored vs. already known. */
    public importMainFlightLog(file: File): Observable<ImportResultDto> {
        const formData = new FormData();
        formData.append('file', file);
        return this.httpClient.post<ImportResultDto>(`${this.apiBaseUrl}/main-flight-log`, formData);
    }
}
