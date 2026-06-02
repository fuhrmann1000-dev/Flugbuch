import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout';

export const routes: Routes = [
    // ── Auth (kein Layout) ───────────────────────────────────────
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login')
            .then(c => c.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./features/auth/register')
            .then(c => c.RegisterComponent)
    },

    // ── App (mit Sidebar-Layout) ─────────────────────────────────
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            {
                path: 'dashboard',
                loadComponent: () => import('./features/dashboard/dashboard')
                    .then(c => c.DashboardComponent)
            },
            {
                path: 'flights',
                loadComponent: () => import('./features/flight-log/flight-list/flight-list')
                    .then(c => c.FlightListComponent)
            },
            {
                path: 'flights/new',
                loadComponent: () => import('./features/flight-entry/flight-entry')
                    .then(c => c.FlightEntryComponent)
            },
            {
                path: 'flights/edit/:id',
                loadComponent: () => import('./features/flight-entry/flight-entry')
                    .then(c => c.FlightEntryComponent)
            },
            {
                path: 'pilots',
                loadComponent: () => import('./features/pilots/pilots')
                    .then(c => c.PilotsComponent)
            },
            {
                path: 'data',
                loadComponent: () => import('./features/data-management/data-management')
                    .then(c => c.DataManagementComponent)
            },
            {
                path: 'profile',
                loadComponent: () => import('./features/profile/profile')
                    .then(c => c.ProfileComponent)
            },
        ]
    },

    { path: '**', redirectTo: 'dashboard' }
];