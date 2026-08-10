import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './login.html',
    styleUrls: ['./login.scss'],
})
export class LoginComponent {
    // Bound to the "E-Mail" field in the template; sent to the backend as
    // the pilot's username (the API has no separate email concept yet).
    public email: string = '';
    public password: string = '';
    public isLoading = signal(false);
    public hasError = signal(false);

    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    /** Calls the real login endpoint; navigates to /flights on success, shows the error banner otherwise. */
    public login(): void {
        this.isLoading.set(true);
        this.hasError.set(false);

        this.authService.login(this.email, this.password).subscribe({
            next: () => this.router.navigate(['/flights']),
            error: () => {
                this.isLoading.set(false);
                this.hasError.set(true);
            }
        });
    }
}
