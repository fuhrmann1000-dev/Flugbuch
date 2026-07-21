import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Pilot {
    id: string; firstName: string; lastName: string;
    email: string; licenseType: string; flightCount: number;
    totalHours: string; status: 'active' | 'inactive';
}

@Component({
    selector: 'app-pilots',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './pilots.html',
    styleUrls: ['./pilots.scss'],
})
export class PilotsComponent {
    public searchTerm = '';
    public showModal = signal(false);
    public editingPilot = signal<Pilot | null>(null);

    // Form fields
    public formFirstName = ''; public formLastName = '';
    public formEmail = ''; public formLicense = '';

    public readonly licenseTypes = ['PPL(A)', 'PPL(B)', 'CPL(A)', 'ATPL', 'SPL', 'Schüler'];

    public readonly allPilots = signal<Pilot[]>([
        { id: '1', firstName: 'Martin', lastName: 'Ackermann', email: 'm.ackermann@edpu.de', licenseType: 'PPL(A)', flightCount: 42, totalHours: '36h 20m', status: 'active' },
        { id: '2', firstName: 'Markus', lastName: 'Hanisch', email: 'hanisch@edpu.de', licenseType: 'SPL', flightCount: 18, totalHours: '14h 55m', status: 'active' },
        { id: '3', firstName: 'Stefan', lastName: 'Martinkat', email: 'martinkat@edpu.de', licenseType: 'PPL(A)', flightCount: 27, totalHours: '22h 10m', status: 'active' },
        { id: '4', firstName: 'Volkmar', lastName: 'Kienöl', email: 'kienoel@edpu.de', licenseType: 'CPL(A)', flightCount: 63, totalHours: '54h 40m', status: 'active' },
        { id: '5', firstName: 'Martin', lastName: 'Odening', email: 'odening@edpu.de', licenseType: 'PPL(A)', flightCount: 31, totalHours: '26h 05m', status: 'active' },
        { id: '6', firstName: 'Jonas', lastName: 'Willemeit', email: 'willemeit@edpu.de', licenseType: 'Schüler', flightCount: 5, totalHours: '4h 15m', status: 'active' },
        { id: '7', firstName: 'Knud', lastName: 'Schäfer', email: 'schaefer@edpu.de', licenseType: 'SPL', flightCount: 12, totalHours: '9h 50m', status: 'inactive' },
    ]);

    public readonly filtered = computed(() => {
        const t = this.searchTerm.toLowerCase();
        if (!t) return this.allPilots();
        return this.allPilots().filter(p =>
            (p.firstName + ' ' + p.lastName).toLowerCase().includes(t) ||
            p.email.toLowerCase().includes(t) || p.licenseType.toLowerCase().includes(t)
        );
    });

    public openNew(): void {
        this.editingPilot.set(null);
        this.formFirstName = ''; this.formLastName = '';
        this.formEmail = ''; this.formLicense = '';
        this.showModal.set(true);
    }

    public openEdit(p: Pilot): void {
        this.editingPilot.set(p);
        this.formFirstName = p.firstName; this.formLastName = p.lastName;
        this.formEmail = p.email; this.formLicense = p.licenseType;
        this.showModal.set(true);
    }

    public saveModal(): void { this.showModal.set(false); }
    public closeModal(): void { this.showModal.set(false); }

    public toggleStatus(p: Pilot): void {
        this.allPilots.update(list =>
            list.map(x => x.id === p.id ? { ...x, status: x.status === 'active' ? 'inactive' : 'active' } : x)
        );
    }
}