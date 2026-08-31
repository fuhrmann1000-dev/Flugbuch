import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { HelperAdminDto, HelperPublicDto, HelperRegistrationRequest } from '../models/helper.model';

/**
 * Talks to /api/v1/helpers (ticket #54). register/confirm/getPublicList work
 * without a login - see SecurityConfig on the backend; getAdminList requires
 * an ADMIN pilot and relies on authInterceptor to attach the JWT like any
 * other authenticated call.
 */
@Injectable({
    providedIn: 'root',
})
export class HelperService {
    private readonly httpClient = inject(HttpClient);
    private readonly apiBaseUrl = `${API_BASE_URL}/helpers`;

    /** Nothing is saved yet - a confirmation email goes out, see confirm(). */
    public register(request: HelperRegistrationRequest): Observable<void> {
        return this.httpClient.post<void>(`${this.apiBaseUrl}/register`, request);
    }

    /** Redeems the link from the confirmation email: creates or updates the helper row. */
    public confirm(token: string): Observable<void> {
        const params = new HttpParams().set('token', token);
        return this.httpClient.get<void>(`${this.apiBaseUrl}/confirm`, { params });
    }

    /** Reduced view: first name, competition, skills, availability - no login required. */
    public getPublicList(): Observable<HelperPublicDto[]> {
        return this.httpClient.get<HelperPublicDto[]>(`${this.apiBaseUrl}/public`);
    }

    /** Full records including contact details - ADMIN only. */
    public getAdminList(): Observable<HelperAdminDto[]> {
        return this.httpClient.get<HelperAdminDto[]>(this.apiBaseUrl);
    }
}
