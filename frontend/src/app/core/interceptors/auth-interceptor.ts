import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Runs on every outgoing HTTP request: if we have a stored (non-expired)
 * JWT, attaches it as "Authorization: Bearer <token>" so the backend's
 * protected endpoints (everything under /api/v1/flights) accept the call.
 * Requests made before any login (e.g. the login/register calls themselves)
 * simply go out without the header - the backend allows those anyway.
 *
 * Also watches responses: if a protected call still comes back 401 (e.g.
 * the token expired mid-session, after AuthService already considered it
 * valid at request time), it clears the stale token and sends the user
 * back to /login instead of leaving them stuck on a broken page.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  const authorizedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = request.url.includes('/auth/');
      if (error.status === 401 && !isAuthEndpoint) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
