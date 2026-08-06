/** Body sent to POST /api/v1/auth/login. */
export interface LoginRequest {
    username: string;
    password: string;
}

/** Body sent to POST /api/v1/auth/register. */
export interface RegisterRequest {
    username: string;
    password: string;
}

/** Body returned by a successful login: the JWT to send as a Bearer token afterwards. */
export interface AuthResponse {
    token: string;
}
