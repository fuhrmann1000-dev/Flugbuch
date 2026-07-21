import { Injectable, inject } from '@angular/core'; // Korrigiert von @angular/common auf @angular/core
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlightLogEntry } from '../models/flight-log-entry.model';

@Injectable({
  providedIn: 'root'
})
export class FlightDataService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = 'http://localhost:8080/api/v1/flights';

  public getAllFlightLogEntries(): Observable<FlightLogEntry[]> {
    return this.httpClient.get<FlightLogEntry[]>(this.apiBaseUrl);
  }

  public createFlightLogEntry(flightLogEntry: Partial<FlightLogEntry>): Observable<FlightLogEntry> {
    return this.httpClient.post<FlightLogEntry>(this.apiBaseUrl, flightLogEntry);
  }
}