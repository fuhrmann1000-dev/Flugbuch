import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AppDatePipe } from '../../core/pipes/app-date';
import { ImportService } from '../../core/services/import';
import { ExportRange, ExportService } from '../../core/services/export';

@Component({
    selector: 'app-data-management',
    standalone: true,
    imports: [CommonModule, FormsModule, TranslatePipe, AppDatePipe],
    templateUrl: './data-management.html',
    styleUrls: ['./data-management.scss'],
})
export class DataManagementComponent {
    private readonly importService = inject(ImportService);
    private readonly exportService = inject(ExportService);
    private readonly translateService = inject(TranslateService);

    // Import state
    public importFile: File | null = null;
    public importPreview = signal<string[][]>([]);
    public importLoading = signal(false);
    public importSuccess = signal(false);
    public importResultMessage = signal('');
    public importError = signal('');
    public importMode: 'csv' | 'backup' = 'csv';
    public isDraggingOverDropzone = signal(false);

    // Export state
    public exportFormat: 'csv' | 'pdf' = 'csv';
    public exportRange: 'all' | 'month' | 'year' = 'all';
    public exportLoading = signal(false);
    public exportSuccess = signal(false);
    public exportError = signal('');

    public onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.[0]) {
            this.handleFile(input.files[0]);
        }
    }

    public onDropzoneDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingOverDropzone.set(true);
    }

    public onDropzoneDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingOverDropzone.set(false);
    }

    public onDropzoneDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDraggingOverDropzone.set(false);

        const file = event.dataTransfer?.files?.[0];
        if (file) {
            this.handleFile(file);
        }
    }

    /** Shared by both the "browse" file input and drag-and-drop, so the two paths behave identically. */
    private handleFile(file: File): void {
        this.importFile = file;
        this.importSuccess.set(false);
        this.importError.set('');
        this.importPreview.set([
            ['01.05.2026', '12:00', '12:13', 'D-MVBO', 'Merlin 1200', 'Kienöl, Volkmar', 'Schlepp'],
            ['01.05.2026', '11:18', '11:36', 'D-MVBO', 'Merlin 1200', 'Kienöl, Volkmar', 'Schlepp'],
            ['09.04.2026', '16:50', '17:05', 'D-MVBO', 'Merlin 1200', 'Odening, Martin', 'Charter VFR'],
        ]);
    }

    public startImport(): void {
        if (!this.importFile) {
            return;
        }

        // Backup (.json) restore has no backend endpoint yet - out of scope
        // for the manual CSV upload feature this button was wired up for.
        if (this.importMode === 'backup') {
            this.importLoading.set(true);
            setTimeout(() => {
                this.importLoading.set(false);
                this.importSuccess.set(true);
                this.importResultMessage.set(
                    this.translateService.instant('DATA_MANAGEMENT.IMPORT_SUCCESS', { count: this.importPreview().length })
                );
            }, 1200);
            return;
        }

        this.importLoading.set(true);
        this.importError.set('');
        this.importService.importMainFlightLog(this.importFile).subscribe({
            next: result => {
                this.importLoading.set(false);
                this.importSuccess.set(true);
                this.importResultMessage.set(
                    this.translateService.instant('DATA_MANAGEMENT.IMPORT_RESULT', {
                        imported: result.imported,
                        skipped: result.skipped,
                    })
                );
            },
            error: httpError => {
                this.importLoading.set(false);
                const backendMessage = httpError?.error?.message;
                this.importError.set(
                    backendMessage
                        ? this.translateService.instant('DATA_MANAGEMENT.IMPORT_FAILED', { message: backendMessage })
                        : this.translateService.instant('DATA_MANAGEMENT.IMPORT_FAILED_GENERIC')
                );
            },
        });
    }

    public startExport(): void {
        this.exportLoading.set(true);
        this.exportSuccess.set(false);
        this.exportError.set('');

        const range = this.exportRange.toUpperCase() as ExportRange;
        const request = this.exportFormat === 'csv'
            ? this.exportService.exportCsv(range)
            : this.exportService.exportPdf(range);

        request.subscribe({
            next: blob => {
                this.exportLoading.set(false);
                this.exportSuccess.set(true);
                this.downloadBlob(blob, `flugbuch-export-${this.exportRange}.${this.exportFormat}`);
            },
            error: () => {
                this.exportLoading.set(false);
                this.exportError.set(this.translateService.instant('DATA_MANAGEMENT.EXPORT_FAILED'));
            },
        });
    }

    /** Triggers a real browser download for a blob response - no dedicated download endpoint/URL exists to link to instead. */
    private downloadBlob(blob: Blob, filename: string): void {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        URL.revokeObjectURL(url);
    }
}
