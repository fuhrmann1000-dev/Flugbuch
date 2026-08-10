import { Component, OnInit, inject, signal, computed, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterLinkActive, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../services/auth';
import { PilotService } from '../../services/pilot';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher';

interface NavItem {
  /** Translation key (e.g. 'NAV.DASHBOARD'), not a display string - resolved via the translate pipe in the template. */
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLinkActive, RouterLink, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './main-layout.html',
  styleUrls: ['./main-layout.scss'],
})
export class MainLayoutComponent implements OnInit {
  public readonly collapsed = signal<boolean>(false);
  public readonly mobileOpen = signal<boolean>(false);

  private readonly pilotService = inject(PilotService);

  // Shown in the profile shortcut at the bottom of the sidebar. Derived from
  // PilotService.currentProfile (a shared signal) rather than a local copy,
  // so uploading a new picture on the Profile page updates the sidebar too -
  // both just read/write the same singleton, no manual syncing needed.
  public readonly profilePicture = computed(() => this.pilotService.currentProfile()?.profilePicture ?? null);
  public readonly profileInitials = computed(() => {
    const profile = this.pilotService.currentProfile();
    const initials = `${profile?.firstName?.[0] ?? ''}${profile?.lastName?.[0] ?? ''}`;
    return initials || 'P'; // 'P' placeholder until the real profile loads (or if it fails to)
  });

  public readonly navItems: NavItem[] = [
    { label: 'NAV.DASHBOARD', icon: 'grid', route: '/dashboard' },
    { label: 'NAV.LOGBOOK', icon: 'book', route: '/flights' },
    { label: 'NAV.ADD_FLIGHT', icon: 'plus', route: '/flights/new' },
    { label: 'NAV.DATA_MANAGEMENT', icon: 'database', route: '/data' },
    { label: 'NAV.SETTINGS', icon: 'settings', route: '/settings' },
  ];

  public ngOnInit(): void {
    // Triggers the initial load into PilotService.currentProfile (see there);
    // best-effort - if this fails, the sidebar just keeps showing the placeholder.
    this.pilotService.getMyProfile().subscribe({ error: () => { /* keep the placeholder */ } });
  }

  public toggleSidebar(): void {
    this.collapsed.update(v => !v);
  }

  public toggleMobile(): void {
    this.mobileOpen.update(v => !v);
  }

  public closeMobile(): void {
    this.mobileOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  public onEscape(): void {
    this.mobileOpen.set(false);
  }

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Clears the stored JWT and sends the pilot back to the login screen. */
  public logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
