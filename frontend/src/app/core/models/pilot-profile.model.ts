/** Shape returned by GET /api/v1/pilots/me and sent to PUT /api/v1/pilots/me. */
export interface PilotProfile {
    id: number;
    /** Login identity - read-only, never sent back on an update. */
    username: string;
    firstName: string | null;
    lastName: string | null;
    email: string | null;
    phone: string | null;
    licenseType: string | null;
    licenseNumber: string | null;
    homeAirfield: string | null;
    /** Base64 data URI (e.g. "data:image/png;base64,..."), or null if none uploaded yet. */
    profilePicture: string | null;
}

/** Body for PUT /api/v1/pilots/me. Picture changes go through a separate endpoint (see below). */
export type UpdatePilotProfileRequest = Omit<PilotProfile, 'id' | 'username' | 'profilePicture'>;

/** Body for PUT /api/v1/pilots/me/password. */
export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

/** Body for PUT /api/v1/pilots/me/picture. */
export interface UpdateProfilePictureRequest {
    profilePicture: string;
}

/** Body for DELETE /api/v1/pilots/me. */
export interface DeleteAccountRequest {
    password: string;
}
