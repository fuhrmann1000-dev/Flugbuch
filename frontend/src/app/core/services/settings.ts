import { Injectable, signal } from '@angular/core';
import {
  DATE_FORMAT_STORAGE_KEY,
  PAGE_SIZE_STORAGE_KEY,
  DEFAULT_DATE_FORMAT,
  DEFAULT_PAGE_SIZE,
  DateFormat,
  PageSizeOption,
  PAGE_SIZE_OPTIONS,
} from '../config/settings-config';

/**
 * Holds the pilot's app-wide display preferences (Settings page): how dates
 * are shown, and the default "entries per page" for tables like the
 * logbook list. Both are persisted in localStorage so they survive a
 * refresh, and exposed as signals so any component picks up a change
 * immediately - no reload needed, same idea as the language switcher.
 */
@Injectable({ providedIn: 'root' })
export class SettingsService {
  public readonly dateFormat = signal<DateFormat>(this.readDateFormat());
  public readonly defaultPageSize = signal<PageSizeOption>(this.readPageSize());

  public setDateFormat(format: DateFormat): void {
    this.dateFormat.set(format);
    localStorage.setItem(DATE_FORMAT_STORAGE_KEY, format);
  }

  public setDefaultPageSize(size: PageSizeOption): void {
    this.defaultPageSize.set(size);
    localStorage.setItem(PAGE_SIZE_STORAGE_KEY, String(size));
  }

  private readDateFormat(): DateFormat {
    const stored = localStorage.getItem(DATE_FORMAT_STORAGE_KEY);
    return stored === 'MM/dd/yyyy' ? 'MM/dd/yyyy' : DEFAULT_DATE_FORMAT;
  }

  private readPageSize(): PageSizeOption {
    const stored = Number(localStorage.getItem(PAGE_SIZE_STORAGE_KEY));
    return PAGE_SIZE_OPTIONS.includes(stored as PageSizeOption) ? (stored as PageSizeOption) : DEFAULT_PAGE_SIZE;
  }
}
