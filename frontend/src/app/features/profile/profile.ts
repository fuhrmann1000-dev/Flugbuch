import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule, TranslatePipe],
    templateUrl: './profile.html',
    styleUrls: ['./profile.scss'],
})
export class ProfileComponent {
    public firstName = 'Max'; public lastName = 'Mustermann';
    public email = 'max.mustermann@edpu.de'; public phone = '+49 177 1234567';
    public licenseType = 'PPL(A)'; public licenseNumber = 'D.PPL(A).12345';
    public homeAirfield = 'EDPU — Altes Lager';
    public saveSuccess = signal(false);

    public readonly licenseTypes = ['PPL(A)', 'PPL(B)', 'CPL(A)', 'ATPL', 'SPL', 'Schüler'];
    // label holds a translation key, resolved via the translate pipe in the template.
    public readonly stats = [
        { label: 'DASHBOARD.STAT_TOTAL_FLIGHTS', value: '42' },
        { label: 'PROFILE.STAT_TOTAL_HOURS', value: '36h 20m' },
        { label: 'DASHBOARD.STAT_TOWS', value: '28' },
        { label: 'PROFILE.STAT_MEMBER_SINCE', value: '2023' },
    ];

    // Password change
    public currentPw = ''; public newPw = ''; public confirmPw = '';

    public save(): void {
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 3000);
    }
}
