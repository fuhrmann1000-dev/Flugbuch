import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlightLogEntry } from '../models/flight-log-entry.model';
import { PageResponse } from '../models/page-response.model';
import { API_BASE_URL } from '../config/api-config';

/** Matches the backend's SortableFlightField enum - see FlightListComponent for which of these the UI actually exposes. */
export type FlightSortField =
  | 'DATE' | 'START_TIME' | 'LANDING_TIME' | 'AIRCRAFT_TYPE' | 'REGISTRATION' | 'PILOT' | 'GUESTS'
  | 'FLIGHT_TYPE' | 'DEPARTURE_AIRFIELD' | 'DESTINATION_AIRFIELD' | 'FLIGHT_DIRECTOR' | 'TOWED_AIRCRAFT'
  | 'TOW_HEIGHT' | 'AMOUNT' | 'REMARKS' | 'FLIGHT_COUNT';

export type SortDirection = 'ASC' | 'DESC';

export interface FlightListQuery {
  page: number;
  size: number;
  sortBy: FlightSortField;
  sortDirection: SortDirection;
}

@Injectable({
  providedIn: 'root'
})
export class FlightDataService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = `${API_BASE_URL}/flights`;

  /**
   * GET /api/v1/flights, real server-side pagination and sorting - the raw
   * PageResponse is returned as-is (not unwrapped) so callers can read
   * totalElements/totalPages to drive real pagination controls instead of
   * only ever seeing the backend's default page.
   */
  public findAll(query: FlightListQuery): Observable<PageResponse<FlightLogEntry>> {
    const params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('sortDirection', query.sortDirection);

    return this.httpClient.get<PageResponse<FlightLogEntry>>(this.apiBaseUrl, { params });
  }

  public createFlightLogEntry(flightLogEntry: Partial<FlightLogEntry>): Observable<FlightLogEntry> {
    return this.httpClient.post<FlightLogEntry>(this.apiBaseUrl, flightLogEntry);
  }
}
