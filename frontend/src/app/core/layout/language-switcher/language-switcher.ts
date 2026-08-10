import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { LANGUAGE_STORAGE_KEY, SUPPORTED_LANGUAGES, SupportedLanguage } from '../../config/i18n-config';

/**
 * Small DE/EN toggle for the sidebar. Switches the whole UI's language at
 * runtime (no reload, no rebuild - ngx-translate just swaps the active
 * translation object) and remembers the choice in localStorage so it
 * survives a refresh. German is the app's source/default language.
 */
@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './language-switcher.html',
  styleUrls: ['./language-switcher.scss'],
})
export class LanguageSwitcherComponent {
  private readonly translateService = inject(TranslateService);

  public readonly languages = SUPPORTED_LANGUAGES;
  public readonly currentLang = signal<SupportedLanguage>(
    (this.translateService.currentLang() as SupportedLanguage) ?? 'de'
  );

  public setLanguage(lang: SupportedLanguage): void {
    if (lang === this.currentLang()) {
      return;
    }
    this.translateService.use(lang);
    localStorage.setItem(LANGUAGE_STORAGE_KEY, lang);
    this.currentLang.set(lang);
  }
}
