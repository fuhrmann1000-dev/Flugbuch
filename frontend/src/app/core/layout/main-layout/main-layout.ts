import { Component, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterLinkActive, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../services/auth';
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
export class MainLayoutComponent {
  public readonly collapsed = signal<boolean>(false);
  public readonly mobileOpen = signal<boolean>(false);

  public readonly navItems: NavItem[] = [
    { label: 'NAV.DASHBOARD', icon: 'grid', route: '/dashboard' },
    { label: 'NAV.LOGBOOK', icon: 'book', route: '/flights' },
    { label: 'NAV.ADD_FLIGHT', icon: 'plus', route: '/flights/new' },
    { label: 'NAV.DATA_MANAGEMENT', icon: 'database', route: '/data' },
    { label: 'NAV.SETTINGS', icon: 'settings', route: '/settings' },
  ];

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
