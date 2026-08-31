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
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, PasswordToggleComponent, LanguageSwitcherComponent],
    templateUrl: './login.html',
    styleUrls: ['./login.scss'],
})
export class LoginComponent {
    public email: string = '';
    public password: string = '';
    public passwordVisible = signal(false);
    public isLoading = signal(false);
    public hasError = signal(false);
    /** Holds a translation key - resolved via the translate pipe in the template. */
    public errorMsg: string = 'LOGIN.ERROR';

    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    /** Calls the real login endpoint; navigates to /flights on success, shows the error banner otherwise. */
    public login(): void {
        this.isLoading.set(true);
        this.hasError.set(false);

        this.authService.login(this.email, this.password).subscribe({
            next: () => this.router.navigate(['/flights']),
            // 429 = too many login attempts from this client (see
            // RateLimitingFilter) - worth a distinct message so it doesn't
            // read as "wrong password" and invite yet another retry.
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                this.errorMsg = error.status === 429 ? 'COMMON.RATE_LIMITED' : 'LOGIN.ERROR';
            }
        });
    }
}
