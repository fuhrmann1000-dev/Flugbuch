import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from '../services/auth';

/**
 * Runs on every outgoing HTTP request: if we have a stored JWT, attaches it
 * as "Authorization: Bearer <token>" so the backend's protected endpoints
 * (everything under /api/v1/flights) accept the call. Requests made before
 * any login (e.g. the login/register calls themselves) simply go out
 * without the header - the backend allows those anyway.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).getToken();

  if (!token) {
    return next(request);
  }

  const authorizedRequest = request.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  });
  return next(authorizedRequest);
};
