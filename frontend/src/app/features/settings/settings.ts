import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { SettingsService } from '../../core/services/settings';
import { LanguageSwitcherComponent } from '../../core/layout/language-switcher/language-switcher';
import { DATE_FORMATS, PAGE_SIZE_OPTIONS, DateFormat, PageSizeOption } from '../../core/config/settings-config';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss'],
})
export class SettingsComponent {
  public readonly settingsService = inject(SettingsService);

  public readonly dateFormats = DATE_FORMATS;
  public readonly pageSizeOptions = PAGE_SIZE_OPTIONS;

  public setDateFormat(format: string): void {
    this.settingsService.setDateFormat(format as DateFormat);
  }

  public setDefaultPageSize(size: string): void {
    this.settingsService.setDefaultPageSize(Number(size) as PageSizeOption);
  }
}
