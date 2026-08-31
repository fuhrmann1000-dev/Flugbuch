import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
    // ── Auth (no layout) ───────────────────────────────────────
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
    {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password')
            .then(c => c.ForgotPasswordComponent)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/reset-password')
            .then(c => c.ResetPasswordComponent)
    },

    // ── Helper sign-up (ticket #54, no layout, no login required) ─
    {
        path: 'helpers',
        loadComponent: () => import('./features/helpers/helpers-public')
            .then(c => c.HelpersPublicComponent)
    },
    {
        // Not "register": one and the same form is used to sign up a new
        // helper AND to edit an already-known one - see HelperFormComponent.
        path: 'helpers/form',
        loadComponent: () => import('./features/helpers/helper-form')
            .then(c => c.HelperFormComponent)
    },
    {
        path: 'helpers/confirm',
        loadComponent: () => import('./features/helpers/helper-confirm')
            .then(c => c.HelperConfirmComponent)
    },

    // ── App (with sidebar layout) ─────────────────────────────────
    {
        path: '',
        component: MainLayoutComponent,
        // canActivate only guards *entering* this parent route (e.g. a fresh
        // navigation straight to /flights). Once MainLayoutComponent is
        // already active, clicking between sidebar links (dashboard -> flights)
        // only re-activates the *child* route, so canActivate alone never
        // runs again - a pilot whose token expired mid-session could still
        // click through to the flight log. canActivateChild re-checks on
        // every child activation, which covers that case too.
        canActivate: [authGuard],
        canActivateChild: [authGuard],
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
                // Open to every logged-in pilot, not ADMIN-only - see
                // HelpersListComponent's doc comment for how the amount of
                // detail shown (and the backend access control behind it)
                // scales with role.
                path: 'helpers/list',
                loadComponent: () => import('./features/helpers-list/helpers-list')
                    .then(c => c.HelpersListComponent)
            },
            {
                path: 'profile',
                loadComponent: () => import('./features/profile/profile')
                    .then(c => c.ProfileComponent)
            },
            {
                path: 'settings',
                loadComponent: () => import('./features/settings/settings')
                    .then(c => c.SettingsComponent)
            },
        ]
    },

    { path: '**', redirectTo: 'dashboard' }
];