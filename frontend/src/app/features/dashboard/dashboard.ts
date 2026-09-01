import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil, catchError, of } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { AppDatePipe } from '../../core/pipes/app-date';
import { FlightDataService } from '../../core/services/flight-data';
import { FlightLogEntry } from '../../core/models/flight-log-entry.model';
import { getDurationHours, formatDuration } from '../../core/utils/flight-duration';
import { getFlightTypeBadgeClass, isTowFlight } from '../../core/utils/flight-type-badge';
import { parseDdMmYyyy, startOfWeek } from '../../core/utils/date-format';

// label/sub/trend hold translation keys, resolved via the translate pipe in the template; trendParams feeds that pipe's interpolation values.
interface StatCard { label: string; value: string; sub: string; subParams?: Record<string, unknown>; icon: string; trend?: string; trendParams?: Record<string, unknown>; trendUp?: boolean; }
interface RecentFlight { date: string; reg: string; pilot: string; type: string; duration: string; typeClass: string; }
interface MonthBucket { year: number; month: number; }

// No aggregation endpoint exists on the backend (total flights, hours,
// active pilots, tow share, monthly chart) - everything below is computed
// client-side from one bulk fetch. 5000 is comfortably more than a single
// club's logbook will ever hold; if it's ever exceeded, totalFlights (read
// straight from the backend's page metadata) stays exact, but the
// hours/tow/monthly/active-pilot aggregates below would silently only
// cover the most recent 5000 flights.
const DASHBOARD_FETCH_SIZE = 5000;
const RECENT_FLIGHTS_COUNT = 5;

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, TranslatePipe, AppDatePipe],
    templateUrl: './dashboard.html',
    styleUrls: ['./dashboard.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
    private readonly flightDataService = inject(FlightDataService);
    private readonly destroy$ = new Subject<void>();

    public readonly isLoading = signal(true);
    public readonly hasError = signal(false);

    public readonly stats = signal<StatCard[]>([]);
    public readonly recentFlights = signal<RecentFlight[]>([]);
    public readonly monthlyHours = signal<number[]>(new Array(12).fill(0));
    public readonly maxHours = signal(1);

    // Rolling 12-month window ending at the current month, rather than a
    // fixed Jan-Dec of the current year - a January dashboard would
    // otherwise show 11 empty upcoming months and nothing from the season
    // that just ended. The chart's last column is always "this month".
    public readonly monthLabels = signal<string[]>([]);
    public readonly chartRangeLabel = signal('');

    // Same abbreviations regardless of active UI language - see the earlier
    // German-audit pass, which deliberately left this as-is; only the real
    // numbers behind this chart were missing, not the month labels.
    private readonly monthAbbreviations = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];

    public ngOnInit(): void {
        this.flightDataService.findAll({ page: 0, size: DASHBOARD_FETCH_SIZE, sortBy: 'DATE', sortDirection: 'DESC' })
            .pipe(
                takeUntil(this.destroy$),
                catchError(() => {
                    this.hasError.set(true);
                    return of(null);
                })
            )
            .subscribe(response => {
                this.isLoading.set(false);
                if (!response) {
                    return;
                }
                this.recentFlights.set(response.content.slice(0, RECENT_FLIGHTS_COUNT).map(f => ({
                    date: f.date,
                    reg: f.registration,
                    pilot: f.pilot,
                    type: f.flightType,
                    duration: formatDuration(f.startTime, f.landingTime),
                    typeClass: getFlightTypeBadgeClass(f.flightType),
                })));
                this.stats.set(this.computeStats(response.content, response.totalElements));
            });
    }

    public ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    /** The 12 (year, month) pairs ending at {@code now}'s month, oldest first - the chart's x-axis. */
    private buildTrailingMonths(now: Date): MonthBucket[] {
        const buckets: MonthBucket[] = [];
        for (let i = 11; i >= 0; i--) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            buckets.push({ year: d.getFullYear(), month: d.getMonth() });
        }
        return buckets;
    }

    private computeStats(flights: FlightLogEntry[], totalElements: number): StatCard[] {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth();
        const weekStart = startOfWeek(now);

        const trailingMonths = this.buildTrailingMonths(now);
        const bucketIndexByKey = new Map<string, number>();
        trailingMonths.forEach((bucket, index) => bucketIndexByKey.set(`${bucket.year}-${bucket.month}`, index));

        let totalHours = 0;
        let hoursThisMonth = 0;
        let flightsThisMonth = 0;
        let towFlights = 0;
        let towFlightsThisWeek = 0;
        const monthlyHours = new Array(12).fill(0);
        const activePilotsThisYear = new Set<string>();

        for (const flight of flights) {
            const date = parseDdMmYyyy(flight.date);
            const hours = getDurationHours(flight.startTime, flight.landingTime);
            totalHours += hours;

            const bucketIndex = bucketIndexByKey.get(`${date.getFullYear()}-${date.getMonth()}`);
            if (bucketIndex !== undefined) {
                monthlyHours[bucketIndex] += hours;
            }

            if (date.getFullYear() === currentYear) {
                activePilotsThisYear.add(flight.pilot.trim().toLowerCase());

                if (date.getMonth() === currentMonth) {
                    hoursThisMonth += hours;
                    flightsThisMonth++;
                }
            }

            if (isTowFlight(flight.flightType)) {
                towFlights++;
                if (date >= weekStart) {
                    towFlightsThisWeek++;
                }
            }
        }

        this.monthLabels.set(trailingMonths.map(b => this.monthAbbreviations[b.month]));
        const first = trailingMonths[0];
        const last = trailingMonths[trailingMonths.length - 1];
        this.chartRangeLabel.set(
            first.year === last.year
                ? `${this.monthAbbreviations[first.month]} - ${this.monthAbbreviations[last.month]} ${last.year}`
                : `${this.monthAbbreviations[first.month]} ${first.year} - ${this.monthAbbreviations[last.month]} ${last.year}`
        );

        this.monthlyHours.set(monthlyHours);
        this.maxHours.set(Math.max(1, ...monthlyHours));

        const towPercentage = totalElements === 0 ? 0 : Math.round((towFlights / totalElements) * 100);

        return [
            {
                label: 'DASHBOARD.STAT_TOTAL_FLIGHTS', value: String(totalElements), icon: '✈',
                sub: 'DASHBOARD.STAT_TOTAL_FLIGHTS_SUB',
                trend: 'DASHBOARD.STAT_TOTAL_FLIGHTS_TREND', trendParams: { count: flightsThisMonth }, trendUp: flightsThisMonth > 0,
            },
            {
                label: 'DASHBOARD.STAT_HOURS', value: `${Math.round(totalHours)}h`, icon: '⏱',
                sub: 'DASHBOARD.STAT_HOURS_SUB',
                trend: 'DASHBOARD.STAT_HOURS_TREND', trendParams: { hours: Math.round(hoursThisMonth) }, trendUp: hoursThisMonth > 0,
            },
            {
                label: 'DASHBOARD.STAT_ACTIVE_PILOTS', value: String(activePilotsThisYear.size), icon: '👤',
                sub: 'DASHBOARD.STAT_ACTIVE_PILOTS_SUB',
            },
            {
                label: 'DASHBOARD.STAT_TOWS', value: String(towFlights), icon: '🪂',
                sub: 'DASHBOARD.STAT_TOWS_SUB', subParams: { percentage: towPercentage },
                trend: 'DASHBOARD.STAT_TOWS_TREND', trendParams: { count: towFlightsThisWeek }, trendUp: towFlightsThisWeek > 0,
            },
        ];
    }
}
