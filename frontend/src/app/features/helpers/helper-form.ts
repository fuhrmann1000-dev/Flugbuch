import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { HelperService } from '../../core/services/helper';
import { AuthService } from '../../core/services/auth';
import { PilotService } from '../../core/services/pilot';
import { ALL_DAYS_OF_WEEK, CompetitionType, DayOfWeek, HelperRegistrationRequest } from '../../core/models/helper.model';
import { LanguageSwitcherComponent } from '../../core/layout/language-switcher/language-switcher';

interface DayOption {
    day: DayOfWeek;
    /** Translation key, e.g. 'HELPERS.DAY_MONDAY' - resolved via the translate pipe in the template. */
    label: string;
    selected: boolean;
}

/**
 * The single public sign-up-or-edit helper form (ticket #54). Deliberately
 * NOT named/labelled "register" anywhere user-facing (file name, route,
 * titles, button text) - it is one and the same form for a brand-new helper
 * and for editing an already-known one, so calling it "sign up" alone would
 * be misleading for the edit case. No login required - reachable from the
 * public helper list. Submitting it never saves anything directly: the
 * backend emails a confirmation link and only applies the data once that
 * link is clicked (see HelperConfirmComponent). Whether the email turns out
 * to belong to a new or an already-known helper is entirely the backend's
 * decision - this form looks the same either way.
 *
 * Logged-in pilots (reached via the in-app Helpers list, see
 * HelpersListComponent) get their name/phone/email prefilled from their
 * Flugbuch profile - a pure convenience, purely client-side: it saves
 * retyping data the app already has, but the submitted email is still what
 * decides create-vs-update on the backend, and the change still has to be
 * confirmed by clicking the emailed link either way.
 */
@Component({
    selector: 'app-helper-form',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, LanguageSwitcherComponent],
    templateUrl: './helper-form.html',
    styleUrls: ['./helpers.scss'],
})
export class HelperFormComponent implements OnInit {
    public firstName = '';
    public lastName = '';
    public phone = '';
    public email = '';
    public competition: CompetitionType | null = null;
    public skills = '';

    public readonly dayOptions = signal<DayOption[]>(
        ALL_DAYS_OF_WEEK.map(day => ({ day, label: `HELPERS.DAY_${day}`, selected: false }))
    );

    public isLoading = signal(false);
    public hasError = signal(false);
    public isSubmitted = signal(false);
    public errorMsg = 'HELPERS.FORM_FAILED';

    public readonly isLoggedIn = signal(false);

    private readonly helperService = inject(HelperService);
    private readonly authService = inject(AuthService);
    private readonly pilotService = inject(PilotService);

    public ngOnInit(): void {
        this.isLoggedIn.set(this.authService.isLoggedIn());
        if (!this.isLoggedIn()) {
            return;
        }

        // Best-effort: if the profile fails to load, the pilot just types
        // their details in by hand like anyone else - not worth an error banner.
        this.pilotService.getMyProfile().subscribe({
            next: profile => {
                this.firstName = profile.firstName ?? '';
                this.lastName = profile.lastName ?? '';
                this.phone = profile.phone ?? '';
                this.email = profile.email ?? '';
            },
            error: () => { /* fall back to a blank form */ }
        });
    }

    /** True once every day is selected - flips the button to "clear all" instead of "mark all". */
    public readonly allDaysSelected = computed(() => this.dayOptions().every(d => d.selected));

    /**
     * One click marks every day available; once all seven are already
     * selected, the same button clears them all instead - so unmarking
     * everything doesn't require seven individual clicks either.
     */
    public toggleAllDays(): void {
        const selectAll = !this.allDaysSelected();
        this.dayOptions.update(days => days.map(d => ({ ...d, selected: selectAll })));
    }

    public toggleDay(day: DayOption): void {
        this.dayOptions.update(days =>
            days.map(d => d.day === day.day ? { ...d, selected: !d.selected } : d)
        );
    }

    public submit(): void {
        if (!this.competition) {
            this.hasError.set(true);
            this.errorMsg = 'HELPERS.COMPETITION_REQUIRED';
            return;
        }

        const request: HelperRegistrationRequest = {
            firstName: this.firstName,
            lastName: this.lastName,
            phone: this.phone,
            email: this.email,
            competition: this.competition,
            skills: this.skills,
            availableDays: this.dayOptions().filter(d => d.selected).map(d => d.day),
        };

        this.isLoading.set(true);
        this.hasError.set(false);

        this.helperService.register(request).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.isSubmitted.set(true);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);
                this.hasError.set(true);
                this.errorMsg = error.status === 429 ? 'COMMON.RATE_LIMITED' : 'HELPERS.FORM_FAILED';
            }
        });
    }
}
