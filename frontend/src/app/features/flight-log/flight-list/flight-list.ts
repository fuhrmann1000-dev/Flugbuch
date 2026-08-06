import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, catchError, of } from 'rxjs';
import { FlightLogEntry } from '../../../core/models/flight-log-entry.model';
import { FlightDataService } from '../../../core/services/flight-data';

@Component({
  selector: 'app-flight-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './flight-list.html',
  styleUrls: ['./flight-list.css'],
})
export class FlightListComponent implements OnInit, OnDestroy {
  private readonly flightDataService = inject(FlightDataService);
  private readonly destroy$ = new Subject<void>();

  // State
  public readonly allFlights = signal<FlightLogEntry[]>([]);
  public readonly isLoading = signal<boolean>(true);
  public readonly hasError = signal<boolean>(false);
  public searchTerm = '';
  public pageSize = 10;
  public currentPage = 1;
  public readonly pageSizeOptions = [10, 25, 50];

  // Computed: filtered list
  public readonly filteredFlights = computed(() => {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) return this.allFlights();
    return this.allFlights().filter(f =>
      f.date.toLowerCase().includes(term) ||
      f.pilot.toLowerCase().includes(term) ||
      f.registration.toLowerCase().includes(term) ||
      f.aircraftType.toLowerCase().includes(term) ||
      f.flightType.toLowerCase().includes(term) ||
      f.departureAirfield.toLowerCase().includes(term) ||
      f.destinationAirfield.toLowerCase().includes(term) ||
      (f.remarks ?? '').toLowerCase().includes(term)
    );
  });

  // Computed: paginated slice
  public readonly pagedFlights = computed(() => {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredFlights().slice(start, start + this.pageSize);
  });

  public readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredFlights().length / this.pageSize))
  );

  public readonly pageNumbers = computed(() => {
    const total = this.totalPages();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages: (number | '…')[] = [1];
    if (this.currentPage > 3) pages.push('…');
    for (let i = Math.max(2, this.currentPage - 1); i <= Math.min(total - 1, this.currentPage + 1); i++) {
      pages.push(i);
    }
    if (this.currentPage < total - 2) pages.push('…');
    pages.push(total);
    return pages;
  });

  public ngOnInit(): void {
    this.flightDataService.getAllFlightLogEntries()
      .pipe(
        takeUntil(this.destroy$),
        catchError(() => {
          this.hasError.set(true);
          return of([]);
        })
      )
      .subscribe(data => {
        this.allFlights.set(data);
        this.isLoading.set(false);
      });
  }

  public ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public onSearchChange(): void {
    this.currentPage = 1;
  }

  public onPageSizeChange(): void {
    this.currentPage = 1;
  }

  public goToPage(page: number | '…'): void {
    if (typeof page === 'number') {
      this.currentPage = Math.max(1, Math.min(page, this.totalPages()));
    }
  }

  public prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  public nextPage(): void {
    if (this.currentPage < this.totalPages()) this.currentPage++;
  }

  public getFlightTypeClass(flightType: string): string {
    switch (flightType?.toLowerCase()) {
      case 'towing':
      case 'schlepp':
        return 'badge-towing';
      case 'charter vfr':
        return 'badge-charter';
      case 'instruction':
        return 'badge-instruction';
      case 'vfr charter':
        return 'badge-charter';
      default:
        return 'badge-default';
    }
  }

  public get showingFrom(): number {
    return this.filteredFlights().length === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
  }

  public get showingTo(): number {
    return Math.min(this.currentPage * this.pageSize, this.filteredFlights().length);
  }

  /**
   * The backend only sends startTime/landingTime ("HH:mm" each) - there's
   * no precomputed duration field - so we work it out here for display.
   */
  public getDuration(startTime: string, landingTime: string): string {
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [landingHours, landingMinutes] = landingTime.split(':').map(Number);
    let totalMinutes = (landingHours * 60 + landingMinutes) - (startHours * 60 + startMinutes);
    if (totalMinutes < 0) {
      totalMinutes += 24 * 60; // landed after midnight
    }
    return `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m`;
  }
}