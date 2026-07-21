import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule],
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
    public readonly stats = [
        { label: 'Flüge gesamt', value: '42' },
        { label: 'Gesamtstunden', value: '36h 20m' },
        { label: 'Schleppflüge', value: '28' },
        { label: 'Mitglied seit', value: '2023' },
    ];

    // Password change
    public currentPw = ''; public newPw = ''; public confirmPw = '';

    public save(): void {
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 3000);
    }
}