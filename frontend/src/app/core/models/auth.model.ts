/** Body sent to POST /api/v1/auth/login. */
export interface LoginRequest {
    email: string;
    password: string;
}

/** Body sent to POST /api/v1/auth/register. Username is a display name only - email is the unique login identity. */
export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

/** Body returned by a successful login: the JWT to send as a Bearer token afterwards. */
export interface AuthResponse {
    token: string;
}

/** Body sent to POST /api/v1/auth/forgot-password. */
export interface ForgotPasswordRequest {
    email: string;
}

/** Body sent to POST /api/v1/auth/reset-password. */
export interface ResetPasswordRequest {
    token: string;
    newPassword: string;
}
