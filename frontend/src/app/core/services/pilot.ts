import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import {
  PilotProfile,
  UpdatePilotProfileRequest,
  ChangePasswordRequest,
  UpdateProfilePictureRequest,
  DeleteAccountRequest,
} from '../models/pilot-profile.model';

/**
 * Talks to /api/v1/pilots/me - everything behind the Profile page.
 *
 * Also doubles as the single source of truth for "what does the logged-in
 * pilot's profile currently look like" via {@link currentProfile}: every
 * method that gets a fresh profile back from the server updates that signal
 * as a side effect. That's what lets the sidebar's avatar (in
 * MainLayoutComponent) pick up a new picture the moment it's uploaded on the
 * Profile page, without the two components needing to know about each other -
 * they both just read/write through this one shared, injectable singleton.
 */
@Injectable({
  providedIn: 'root',
})
export class PilotService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = `${API_BASE_URL}/pilots`;

  public readonly currentProfile = signal<PilotProfile | null>(null);

  public getMyProfile(): Observable<PilotProfile> {
    return this.httpClient.get<PilotProfile>(`${this.apiBaseUrl}/me`)
      .pipe(tap(profile => this.currentProfile.set(profile)));
  }

  public updateMyProfile(request: UpdatePilotProfileRequest): Observable<PilotProfile> {
    return this.httpClient.put<PilotProfile>(`${this.apiBaseUrl}/me`, request)
      .pipe(tap(profile => this.currentProfile.set(profile)));
  }

  public updateProfilePicture(request: UpdateProfilePictureRequest): Observable<PilotProfile> {
    return this.httpClient.put<PilotProfile>(`${this.apiBaseUrl}/me/picture`, request)
      .pipe(tap(profile => this.currentProfile.set(profile)));
  }

  public changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.httpClient.put<void>(`${this.apiBaseUrl}/me/password`, request);
  }

  /**
   * DELETE with a request body is a bit unusual but well-supported by
   * HttpClient - needed here because the pilot must re-confirm their
   * password before the account is actually deleted.
   */
  public deleteMyAccount(request: DeleteAccountRequest): Observable<void> {
    return this.httpClient.request<void>('DELETE', `${this.apiBaseUrl}/me`, { body: request })
      .pipe(tap(() => this.currentProfile.set(null)));
  }
}
