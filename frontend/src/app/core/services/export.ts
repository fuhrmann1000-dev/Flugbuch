import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';

export type ExportRange = 'ALL' | 'YEAR' | 'MONTH';

/**
 * Talks to /api/v1/exports - the "Export Data" card on the Data Management
 * page. Requires a logged-in pilot (no particular role); authInterceptor
 * attaches the JWT like any other authenticated call. Both endpoints return
 * a downloadable file body, so responseType is 'blob' rather than JSON.
 */
@Injectable({
    providedIn: 'root',
})
export class ExportService {
    private readonly httpClient = inject(HttpClient);
    private readonly apiBaseUrl = `${API_BASE_URL}/exports`;

    /** Same column layout the manual CSV import expects, so it can be re-imported as-is. */
    public exportCsv(range: ExportRange): Observable<Blob> {
        return this.httpClient.get(`${this.apiBaseUrl}/csv`, { params: { range }, responseType: 'blob' });
    }

    /** Renders the same "Flugbuch" PDF layout used for the daily printout, for the chosen range instead of a single day. */
    public exportPdf(range: ExportRange): Observable<Blob> {
        return this.httpClient.get(`${this.apiBaseUrl}/pdf`, { params: { range }, responseType: 'blob' });
    }
}
