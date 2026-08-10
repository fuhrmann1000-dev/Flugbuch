import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
    templateUrl: './register.html',
    styleUrls: ['./login.scss'],
})
export class RegisterComponent {
    public firstName: string = '';
    public lastName: string = '';
    // Bound to the "E-Mail" field; sent to the backend as the pilot's
    // username. firstName/lastName aren't sent - the backend doesn't have
    // fields for a display name yet, only username + password.
    public email: string = '';
    public password: string = '';
    public passwordConfirm: string = '';
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

        this.authService.register(this.email, this.password).subscribe({
            next: () => this.router.navigate(['/login']),
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                // Holds a translation key, not display text - the template resolves it
                // via the translate pipe so the message stays correct across language switches.
                this.errorMsg = error.status === 409
                    ? 'REGISTER.USERNAME_TAKEN'
                    : 'REGISTER.FAILED';
            }
        });
    }
}
