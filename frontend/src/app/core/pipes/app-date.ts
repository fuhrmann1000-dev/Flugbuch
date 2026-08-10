import { Pipe, PipeTransform, inject } from '@angular/core';
import { SettingsService } from '../services/settings';

/**
 * Reformats a "dd.MM.yyyy" date string (the format the backend/mock data
 * uses everywhere) according to the pilot's date-format preference from
 * the Settings page. Marked impure so it re-renders immediately when the
 * setting changes, without needing a page reload.
 */
@Pipe({
  name: 'appDate',
  standalone: true,
  pure: false,
})
export class AppDatePipe implements PipeTransform {
  private readonly settingsService = inject(SettingsService);

  public transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const parts = value.split('.');
    if (parts.length !== 3) {
      return value; // not in the expected dd.MM.yyyy shape - show as-is
    }
    const [day, month, year] = parts;
    return this.settingsService.dateFormat() === 'MM/dd/yyyy'
      ? `${month}/${day}/${year}`
      : `${day}.${month}.${year}`;
  }
}
