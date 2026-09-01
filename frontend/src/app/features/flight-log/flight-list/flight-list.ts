import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil, catchError, of } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { FlightLogEntry } from '../../../core/models/flight-log-entry.model';
import { FlightDataService, FlightSortField, SortDirection } from '../../../core/services/flight-data';
import { SettingsService } from '../../../core/services/settings';
import { AppDatePipe } from '../../../core/pipes/app-date';
import { formatDuration } from '../../../core/utils/flight-duration';
import { getFlightTypeBadgeClass } from '../../../core/utils/flight-type-badge';

/** A curated subset of the backend's sortable fields - one per visible table column, to keep the sort menu short. */
interface SortOption {
  field: FlightSortField;
  labelKey: string;
}

// There's no full-text search endpoint on the backend (only pilot/aircraftType/
// registration support partial matching, each separately - see
// FlightSpecifications), so an active search fetches "everything" once and
// filters/paginates client-side across all the columns the search box always
// covered. This is comfortably enough for a single club's logbook.
const SEARCH_FETCH_SIZE = 5000;

@Component({
  selector: 'app-flight-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, AppDatePipe],
  templateUrl: './flight-list.html',
  styleUrls: ['./flight-list.css'],
})
export class FlightListComponent implements OnInit, OnDestroy {
  private readonly flightDataService = inject(FlightDataService);
  private readonly settingsService = inject(SettingsService);
  private readonly destroy$ = new Subject<void>();

  public readonly sortOptions: SortOption[] = [
    { field: 'DATE', labelKey: 'DASHBOARD.TABLE_DATE' },
    { field: 'PILOT', labelKey: 'DASHBOARD.TABLE_PILOT' },
    { field: 'REGISTRATION', labelKey: 'FLIGHT_LIST.TABLE_REG_MODEL' },
    { field: 'FLIGHT_TYPE', labelKey: 'DASHBOARD.TABLE_TYPE' },
    { field: 'FLIGHT_COUNT', labelKey: 'FLIGHT_LIST.SORT_LANDINGS' },
  ];

  // State - everything the fetch/filter/pagination logic depends on is a
  // real signal, not a plain field, so computed() below actually reacts to
  // it (a plain field read inside computed() is invisible to its dependency
  // tracking and silently goes stale - the bug this page had before).
  public readonly rows = signal<FlightLogEntry[]>([]);
  public readonly isLoading = signal(true);
  public readonly hasError = signal(false);
  public readonly searchTerm = signal('');
  public readonly pageSize = signal<number>(this.settingsService.defaultPageSize());
  public readonly currentPage = signal(1);
  public readonly sortField = signal<FlightSortField>('DATE');
  public readonly sortDirection = signal<SortDirection>('DESC');
  public readonly isSortMenuOpen = signal(false);
  public readonly pageSizeOptions = [10, 25, 50];

  // Only set (and relevant) in browse mode - the exact totals the backend
  // computed for the whole table, not just the currently loaded page.
  private readonly serverTotalElements = signal(0);
  private readonly serverTotalPages = signal(1);

  public readonly isSearching = computed(() => this.searchTerm().trim().length > 0);

  /** Browse mode: rows() is already exactly the requested server page - nothing left to filter. Search mode: rows() holds the full dataset. */
  public readonly filteredFlights = computed(() => {
    if (!this.isSearching()) {
      return this.rows();
    }
    const term = this.searchTerm().toLowerCase().trim();
    return this.rows().filter(f =>
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

  /** Browse mode: server already returned exactly one page, so this is filteredFlights() unchanged. Search mode: slice locally. */
  public readonly pagedFlights = computed(() => {
    if (!this.isSearching()) {
      return this.filteredFlights();
    }
    const start = (this.currentPage() - 1) * this.pageSize();
    return this.filteredFlights().slice(start, start + this.pageSize());
  });

  public readonly totalCount = computed(() =>
    this.isSearching() ? this.filteredFlights().length : this.serverTotalElements()
  );

  public readonly totalPages = computed(() =>
    this.isSearching()
      ? Math.max(1, Math.ceil(this.filteredFlights().length / this.pageSize()))
      : Math.max(1, this.serverTotalPages())
  );

  public readonly pageNumbers = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages: (number | '…')[] = [1];
    if (current > 3) pages.push('…');
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i);
    }
    if (current < total - 2) pages.push('…');
    pages.push(total);
    return pages;
  });

  public ngOnInit(): void {
    this.loadFlights();
  }

  public ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Fetches from the backend. Browse mode asks for exactly the page/size/
   * sort the user picked; search mode asks for the whole (sorted) dataset
   * once, since filtering has to happen client-side either way.
   */
  private loadFlights(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    const query = this.isSearching()
      ? { page: 0, size: SEARCH_FETCH_SIZE, sortBy: this.sortField(), sortDirection: this.sortDirection() }
      : { page: this.currentPage() - 1, size: this.pageSize(), sortBy: this.sortField(), sortDirection: this.sortDirection() };

    this.flightDataService.findAll(query)
      .pipe(
        takeUntil(this.destroy$),
        catchError(() => {
          this.hasError.set(true);
          return of(null)
        })
      )
      .subscribe(response => {
        this.isLoading.set(false);
        if (!response) {
          return;
        }
        this.rows.set(response.content);
        this.serverTotalElements.set(response.totalElements);
        this.serverTotalPages.set(response.totalPages);
      });
  }

  public onSearchTermChange(value: string): void {
    this.searchTerm.set(value);
    this.currentPage.set(1);
    this.loadFlights();
  }

  public onPageSizeChange(value: number): void {
    this.pageSize.set(Number(value));
    this.currentPage.set(1);
    this.loadFlights();
  }

  public goToPage(page: number | '…'): void {
    if (typeof page !== 'number') {
      return;
    }
    this.currentPage.set(Math.max(1, Math.min(page, this.totalPages())));
    // Search mode already has the full dataset loaded - paging through it is
    // purely a local slice, no need to hit the backend again.
    if (!this.isSearching()) {
      this.loadFlights();
    }
  }

  public prevPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  public nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  public toggleSortMenu(): void {
    this.isSortMenuOpen.update(open => !open);
  }

  public closeSortMenu(): void {
    this.isSortMenuOpen.set(false);
  }

  /** Picking the already-active field flips its direction; picking a new one starts ascending. */
  public selectSort(field: FlightSortField): void {
    if (this.sortField() === field) {
      this.sortDirection.set(this.sortDirection() === 'ASC' ? 'DESC' : 'ASC');
    } else {
      this.sortField.set(field);
      this.sortDirection.set('ASC');
    }
    this.isSortMenuOpen.set(false);
    this.currentPage.set(1);
    this.loadFlights();
  }

  public getFlightTypeClass(flightType: string): string {
    return getFlightTypeBadgeClass(flightType);
  }

  public get showingFrom(): number {
    return this.totalCount() === 0 ? 0 : (this.currentPage() - 1) * this.pageSize() + 1;
  }

  public get showingTo(): number {
    return Math.min(this.currentPage() * this.pageSize(), this.totalCount());
  }

  public getDuration(startTime: string, landingTime: string): string {
    return formatDuration(startTime, landingTime);
  }
}
