import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth';
import { PasswordToggleComponent } from '../../shared/password-toggle/password-toggle';
import { LanguageSwitcherComponent } from '../../core/layout/language-switcher/language-switcher';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, PasswordToggleComponent, LanguageSwitcherComponent],
    templateUrl: './register.html',
    styleUrls: ['./login.scss'],
})
export class RegisterComponent {
    public firstName: string = '';
    public lastName: string = '';
    // username is just a display name (not unique); email is the real login
    // identity. firstName/lastName aren't sent - the backend's Profile page
    // is where those get filled in.
    public username: string = '';
    public email: string = '';
    public password: string = '';
    public passwordConfirm: string = '';
    public passwordVisible = signal(false);
    public passwordConfirmVisible = signal(false);
    public isLoading = signal(false);
    public hasError = signal(false);
    public errorMsg: string = '';

    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    /** Calls the real register endpoint. On success sends the pilot to /login - registering doesn't log them in automatically. */
    public register(): void {
        if (this.password !== this.passwordConfirm) {
            this.hasError.set(true);
            this.errorMsg = 'REGISTER.PASSWORD_MISMATCH';
            return;
        }

        this.isLoading.set(true);
        this.hasError.set(false);

        this.authService.register(this.username, this.email, this.password).subscribe({
            next: () => this.router.navigate(['/login']),
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                // Holds a translation key, not display text - the template resolves it
                // via the translate pipe so the message stays correct across language switches.
                // 429 = too many registration attempts from this client (see RateLimitingFilter).
                if (error.status === 409) {
                    this.errorMsg = 'REGISTER.EMAIL_TAKEN';
                } else if (error.status === 429) {
                    this.errorMsg = 'COMMON.RATE_LIMITED';
                } else {
                    this.errorMsg = 'REGISTER.FAILED';
                }
            }
        });
    }
}
