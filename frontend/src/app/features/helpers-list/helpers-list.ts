import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { HelperService } from '../../core/services/helper';
import { AuthService } from '../../core/services/auth';
import { ALL_DAYS_OF_WEEK, DayOfWeek, HelperAdminDto, HelperPublicDto } from '../../core/models/helper.model';

/**
 * In-app helper listing (ticket #54), reachable from the sidebar by every
 * logged-in pilot - not ADMIN-only. Which data it shows scales with role:
 * ADMIN pilots get the full contact details (GET /api/v1/helpers, backend-
 * enforced ADMIN-only - see SecurityConfig), everyone else sees the same
 * reduced view as the public listing (GET /api/v1/helpers/public). Either
 * way there's a link to the shared sign-up-or-edit form, so a pilot who is
 * ALSO a competition helper doesn't have to leave the app and hunt for the
 * public page to register or update their own helper details.
 */
@Component({
    selector: 'app-helpers-list',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
    templateUrl: './helpers-list.html',
    styleUrls: ['./helpers-list.scss'],
})
export class HelpersListComponent implements OnInit {
    public readonly allDays = ALL_DAYS_OF_WEEK;
    public readonly isAdmin = signal(false);
    public readonly adminHelpers = signal<HelperAdminDto[]>([]);
    public readonly publicHelpers = signal<HelperPublicDto[]>([]);
    public readonly isLoading = signal(true);
    public readonly hasError = signal(false);
    public searchTerm = '';

    private readonly helperService = inject(HelperService);
    private readonly authService = inject(AuthService);

    public ngOnInit(): void {
        const admin = this.authService.isAdmin();
        this.isAdmin.set(admin);

        const request$ = admin ? this.helperService.getAdminList() : this.helperService.getPublicList();
        request$.subscribe({
            next: helpers => {
                if (admin) {
                    this.adminHelpers.set(helpers as HelperAdminDto[]);
                } else {
                    this.publicHelpers.set(helpers as HelperPublicDto[]);
                }
                this.isLoading.set(false);
            },
            error: () => {
                this.hasError.set(true);
                this.isLoading.set(false);
            }
        });
    }

    public isAvailable(helper: HelperAdminDto | HelperPublicDto, day: DayOfWeek): boolean {
        return helper.availableDays.includes(day);
    }

    public get filteredAdmin(): HelperAdminDto[] {
        const term = this.searchTerm.toLowerCase();
        if (!term) {
            return this.adminHelpers();
        }
        return this.adminHelpers().filter(h =>
            `${h.firstName} ${h.lastName}`.toLowerCase().includes(term) ||
            h.email.toLowerCase().includes(term) ||
            h.skills?.toLowerCase().includes(term)
        );
    }

    public get filteredPublic(): HelperPublicDto[] {
        const term = this.searchTerm.toLowerCase();
        if (!term) {
            return this.publicHelpers();
        }
        return this.publicHelpers().filter(h =>
            h.firstName.toLowerCase().includes(term) || h.skills?.toLowerCase().includes(term)
        );
    }
}
