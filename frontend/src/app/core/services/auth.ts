import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { AuthResponse, ForgotPasswordRequest, LoginRequest, RegisterRequest, ResetPasswordRequest } from '../models/auth.model';

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
  public login(email: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = { email, password };
    return this.httpClient.post<AuthResponse>(`${this.apiBaseUrl}/login`, request)
      .pipe(tap(response => this.storeToken(response.token)));
  }

  /** Calls POST /auth/register. Does not log the pilot in automatically - they still have to log in afterwards. */
  public register(username: string, email: string, password: string): Observable<void> {
    const request: RegisterRequest = { username, email, password };
    return this.httpClient.post<void>(`${this.apiBaseUrl}/register`, request);
  }

  /** Calls POST /auth/forgot-password. Always resolves, whether or not the account exists. */
  public forgotPassword(email: string): Observable<void> {
    const request: ForgotPasswordRequest = { email };
    return this.httpClient.post<void>(`${this.apiBaseUrl}/forgot-password`, request);
  }

  /** Calls POST /auth/reset-password with the token from the emailed link. */
  public resetPassword(token: string, newPassword: string): Observable<void> {
    const request: ResetPasswordRequest = { token, newPassword };
    return this.httpClient.post<void>(`${this.apiBaseUrl}/reset-password`, request);
  }

  /** Discards the stored token, ending the session on this device. */
  public logout(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  /** The current JWT, or null if nobody is logged in. */
  public getToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  /**
   * True only if there's a token AND it hasn't expired yet. A merely
   * *present* token isn't enough - JWTs carry their own expiration (1h by
   * default here) and the browser has no way to know it expired other than
   * checking that timestamp itself, since the token is never invalidated
   * server-side. An expired token found here is discarded on the spot.
   */
  public isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    if (this.isTokenExpired(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payloadSegment = token.split('.')[1];
      const base64 = payloadSegment.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true; // malformed token - treat it as invalid/expired
    }
  }

  private storeToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }
}
