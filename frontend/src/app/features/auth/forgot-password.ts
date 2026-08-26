import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
    templateUrl: './forgot-password.html',
    styleUrls: ['./login.scss'],
})
export class ForgotPasswordComponent {
    public email: string = '';
    public isLoading = signal(false);
    public hasError = signal(false);
    public isSubmitted = signal(false);
    public errorMsg: string = 'FORGOT_PASSWORD.FAILED';

    private readonly authService = inject(AuthService);

    /** The backend always responds the same way regardless of whether the account exists - see AuthController. */
    public submit(): void {
        this.isLoading.set(true);
        this.hasError.set(false);

        this.authService.forgotPassword(this.email).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.isSubmitted.set(true);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                this.errorMsg = error.status === 429 ? 'COMMON.RATE_LIMITED' : 'FORGOT_PASSWORD.FAILED';
            }
        });
    }
}
