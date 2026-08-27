import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { FlightLogEntry } from '../models/flight-log-entry.model';
import { PageResponse } from '../models/page-response.model';
import { API_BASE_URL } from '../config/api-config';

@Injectable({
  providedIn: 'root'
})
export class FlightDataService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = `${API_BASE_URL}/flights`;

  /**
   * GET /api/v1/flights returns a PageResponse (content + pagination info),
   * not a bare array. For now we only need the list itself, so we unwrap
   * .content here - callers still just get a flat FlightLogEntry[].
   *
   * Sorted by date, most recent first, by default - the backend leaves
   * entries unsorted (insertion order) unless a sort is requested, which
   * isn't a real chronological order for imported/edited data.
   */
  public getAllFlightLogEntries(): Observable<FlightLogEntry[]> {
    const params = new HttpParams()
      .set('sortBy', 'DATE')
      .set('sortDirection', 'DESC');

    return this.httpClient.get<PageResponse<FlightLogEntry>>(this.apiBaseUrl, { params })
      .pipe(map(response => response.content));
  }

  public createFlightLogEntry(flightLogEntry: Partial<FlightLogEntry>): Observable<FlightLogEntry> {
    return this.httpClient.post<FlightLogEntry>(this.apiBaseUrl, flightLogEntry);
  }
}
