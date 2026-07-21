import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './register.html',
    styleUrls: ['./login.scss'],
})
export class RegisterComponent {
    public firstName: string = '';
    public lastName: string = '';
    public email: string = '';
    public password: string = '';
    public passwordConfirm: string = '';
    public isLoading = signal(false);
    public hasError = signal(false);
    public errorMsg: string = '';

    constructor(private router: Router) { }

    public register(): void {
        if (this.password !== this.passwordConfirm) {
            this.hasError.set(true);
            this.errorMsg = 'Passwörter stimmen nicht überein.';
            return;
        }
        this.isLoading.set(true);
        setTimeout(() => this.router.navigate(['/flights']), 900);
    }
}