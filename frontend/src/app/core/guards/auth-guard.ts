import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Blocks navigation to any route it's attached to unless a JWT is stored,
 * redirecting to /login instead. This is a UX convenience, not the real
 * security boundary - the backend rejects unauthenticated calls regardless
 * (see JwtAuthenticationFilter), so someone bypassing this guard still
 * couldn't fetch real data.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
