import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { HelperService } from '../../core/services/helper';
import { LanguageSwitcherComponent } from '../../core/layout/language-switcher/language-switcher';

/**
 * Landing page for the confirmation link sent by HelperConfirmationMailService
 * (ticket #54). Reads the token from the query string and redeems it - this
 * is the only place a helper sign-up/edit actually gets written to the
 * database, see HelperController#confirm on the backend.
 */
@Component({
    selector: 'app-helper-confirm',
    standalone: true,
    imports: [CommonModule, RouterLink, TranslatePipe, LanguageSwitcherComponent],
    templateUrl: './helper-confirm.html',
    styleUrls: ['./helpers.scss'],
})
export class HelperConfirmComponent implements OnInit {
    public readonly isLoading = signal(true);
    public readonly isConfirmed = signal(false);
    public readonly hasError = signal(false);

    private readonly route = inject(ActivatedRoute);
    private readonly helperService = inject(HelperService);

    public ngOnInit(): void {
        const token = this.route.snapshot.queryParamMap.get('token');
        if (!token) {
            this.isLoading.set(false);
            this.hasError.set(true);
            return;
        }

        this.helperService.confirm(token).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.isConfirmed.set(true);
            },
            error: () => {
                this.isLoading.set(false);
                this.hasError.set(true);
            }
        });
    }
}
