import { Component, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterLinkActive, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLinkActive, RouterLink],
  templateUrl: './main-layout.html',
  styleUrls: ['./main-layout.scss'],
})
export class MainLayoutComponent {
  public readonly collapsed = signal<boolean>(false);
  public readonly mobileOpen = signal<boolean>(false);

  public readonly navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'grid', route: '/dashboard' },
    { label: 'Logbook Entries', icon: 'book', route: '/logbook' },
    { label: 'Add New Flight', icon: 'plus', route: '/flight/new' },
    { label: 'Data Management', icon: 'database', route: '/data' },
    { label: 'Settings', icon: 'settings', route: '/settings' },
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