import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './login.html',
    styleUrls: ['./login.scss'],
})
export class LoginComponent {
    public email: string = '';
    public password: string = '';
    public isLoading = signal(false);
    public hasError = signal(false);

    constructor(private router: Router) { }

    public login(): void {
        this.isLoading.set(true);
        this.hasError.set(false);
        // Mock: simulate login delay
        setTimeout(() => {
            if (this.email && this.password) {
                this.router.navigate(['/flights']);
            } else {
                this.isLoading.set(false);
                this.hasError.set(true);
            }
        }, 800);
    }
}