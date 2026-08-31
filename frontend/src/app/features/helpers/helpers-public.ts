import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { HelperService } from '../../core/services/helper';
import { ALL_DAYS_OF_WEEK, DayOfWeek, HelperPublicDto } from '../../core/models/helper.model';
import { LanguageSwitcherComponent } from '../../core/layout/language-switcher/language-switcher';

/**
 * Public, reduced listing of confirmed competition helpers (ticket #54): no
 * login required. Only shows what GET /helpers/public actually returns -
 * first name, competition, skills, availability - never contact details,
 * see HelperAdminComponent for that.
 */
@Component({
    selector: 'app-helpers-public',
    standalone: true,
    imports: [CommonModule, RouterLink, TranslatePipe, LanguageSwitcherComponent],
    templateUrl: './helpers-public.html',
    styleUrls: ['./helpers.scss'],
})
export class HelpersPublicComponent implements OnInit {
    public readonly allDays = ALL_DAYS_OF_WEEK;
    public readonly helpers = signal<HelperPublicDto[]>([]);
    public readonly isLoading = signal(true);
    public readonly hasError = signal(false);

    private readonly helperService = inject(HelperService);

    public ngOnInit(): void {
        this.helperService.getPublicList().subscribe({
            next: helpers => {
                this.helpers.set(helpers);
                this.isLoading.set(false);
            },
            error: () => {
                this.hasError.set(true);
                this.isLoading.set(false);
            }
        });
    }

    public isAvailable(helper: HelperPublicDto, day: DayOfWeek): boolean {
        return helper.availableDays.includes(day);
    }
}
