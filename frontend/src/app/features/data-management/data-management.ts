import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-data-management',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './data-management.html',
    styleUrls: ['./data-management.scss'],
})
export class DataManagementComponent {
    // Import state
    public importFile: File | null = null;
    public importPreview = signal<string[][]>([]);
    public importLoading = signal(false);
    public importSuccess = signal(false);
    public importMode: 'csv' | 'backup' = 'csv';

    // Export state
    public exportFormat: 'csv' | 'pdf' = 'csv';
    public exportRange: 'all' | 'month' | 'year' = 'all';
    public exportLoading = signal(false);
    public exportSuccess = signal(false);

    public onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.[0]) {
            this.importFile = input.files[0];
            this.importPreview.set([
                ['01.05.2026', '12:00', '12:13', 'D-MVBO', 'Merlin 1200', 'Kienöl, Volkmar', 'Schlepp'],
                ['01.05.2026', '11:18', '11:36', 'D-MVBO', 'Merlin 1200', 'Kienöl, Volkmar', 'Schlepp'],
                ['09.04.2026', '16:50', '17:05', 'D-MVBO', 'Merlin 1200', 'Odening, Martin', 'Charter VFR'],
            ]);
        }
    }

    public startImport(): void {
        this.importLoading.set(true);
        setTimeout(() => { this.importLoading.set(false); this.importSuccess.set(true); }, 1200);
    }

    public startExport(): void {
        this.exportLoading.set(true);
        setTimeout(() => { this.exportLoading.set(false); this.exportSuccess.set(true); }, 1000);
    }
}