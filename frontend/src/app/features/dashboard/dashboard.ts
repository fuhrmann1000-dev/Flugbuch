import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface StatCard { label: string; value: string; sub: string; icon: string; trend?: string; trendUp?: boolean; }
interface RecentFlight { date: string; reg: string; pilot: string; type: string; duration: string; typeClass: string; }

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.html',
    styleUrls: ['./dashboard.scss'],
})
export class DashboardComponent {
    public readonly stats: StatCard[] = [
        { label: 'Flüge gesamt', value: '174', sub: 'Alle Zeiträume', icon: '✈', trend: '+12 diesen Monat', trendUp: true },
        { label: 'Flugstunden', value: '142h', sub: 'Kumuliert', icon: '⏱', trend: '+8h diesen Monat', trendUp: true },
        { label: 'Aktive Piloten', value: '9', sub: 'Registriert', icon: '👤', },
        { label: 'Schleppflüge', value: '98', sub: '56% aller Flüge', icon: '🪂', trend: '+5 diese Woche', trendUp: true },
    ];

    public readonly recentFlights: RecentFlight[] = [
        { date: '01.05.2026', reg: 'D-MVBO', pilot: 'Kienöl, Volkmar', type: 'Schlepp', duration: '0h 13m', typeClass: 'badge-towing' },
        { date: '01.05.2026', reg: 'D-MVBO', pilot: 'Kienöl, Volkmar', type: 'Schlepp', duration: '0h 18m', typeClass: 'badge-towing' },
        { date: '09.04.2026', reg: 'D-MVBO', pilot: 'Odening, Martin', type: 'Charter VFR', duration: '0h 15m', typeClass: 'badge-charter' },
        { date: '09.04.2026', reg: 'D-MVBO', pilot: 'Odening, Martin', type: 'Schlepp DoSi', duration: '0h 10m', typeClass: 'badge-towing' },
        { date: '09.05.2026', reg: 'D-MVBO', pilot: 'Odening, Martin', type: 'Schlepp', duration: '0h 09m', typeClass: 'badge-towing' },
    ];

    public readonly monthlyHours = [4.2, 6.1, 3.8, 8.4, 11.2, 9.7, 7.3, 12.1, 10.5, 8.9, 6.4, 14.2];
    public readonly months = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
    public readonly maxHours = 15;
}