import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth';
import { PasswordToggleComponent } from '../../shared/password-toggle/password-toggle';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, PasswordToggleComponent],
    templateUrl: './reset-password.html',
    styleUrls: ['./login.scss'],
})
export class ResetPasswordComponent {
    // Mirrors the backend's @Pattern on ResetPasswordRequest.newPassword.
    private static readonly PASSWORD_COMPLEXITY = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;

    public newPassword: string = '';
    public confirmPassword: string = '';
    public newPasswordVisible = signal(false);
    public confirmPasswordVisible = signal(false);
    public isLoading = signal(false);
    public hasError = signal(false);
    public isSubmitted = signal(false);
    public errorMsg: string = '';

    private readonly token: string | null;
    private readonly authService = inject(AuthService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    constructor() {
        this.token = this.route.snapshot.queryParamMap.get('token');
    }

    public hasToken(): boolean {
        return !!this.token;
    }

    public submit(): void {
        this.hasError.set(false);

        if (this.newPassword !== this.confirmPassword) {
            this.hasError.set(true);
            this.errorMsg = 'RESET_PASSWORD.PASSWORD_MISMATCH';
            return;
        }
        if (!ResetPasswordComponent.PASSWORD_COMPLEXITY.test(this.newPassword)) {
            this.hasError.set(true);
            this.errorMsg = 'RESET_PASSWORD.PASSWORD_TOO_SIMPLE';
            return;
        }

        this.isLoading.set(true);

        this.authService.resetPassword(this.token!, this.newPassword).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.isSubmitted.set(true);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                this.errorMsg = error.status === 429 ? 'COMMON.RATE_LIMITED' : 'RESET_PASSWORD.INVALID_LINK';
            }
        });
    }

    public goToLogin(): void {
        this.router.navigate(['/login']);
    }
}
