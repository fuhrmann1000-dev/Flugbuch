import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

const TOKEN_STORAGE_KEY = 'flugbuch_jwt';

/**
 * Handles login/registration against the backend and holds on to the JWT
 * afterwards (in localStorage, so a page refresh doesn't log the user out).
 * The actual attaching of the token to outgoing requests happens in
 * {@link authInterceptor}, not here.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = `${API_BASE_URL}/auth`;

  /** Calls POST /auth/login; on success, stores the returned JWT. */
  public login(username: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = { username, password };
    return this.httpClient.post<AuthResponse>(`${this.apiBaseUrl}/login`, request)
      .pipe(tap(response => this.storeToken(response.token)));
  }

  /** Calls POST /auth/register. Does not log the pilot in automatically - they still have to log in afterwards. */
  public register(username: string, password: string): Observable<void> {
    const request: RegisterRequest = { username, password };
    return this.httpClient.post<void>(`${this.apiBaseUrl}/register`, request);
  }

  /** Discards the stored token, ending the session on this device. */
  public logout(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  /** The current JWT, or null if nobody is logged in. */
  public getToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  public isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  private storeToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }
}
