import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth-interceptor';
import { LANGUAGE_STORAGE_KEY } from './core/config/i18n-config';

// Deutsch ist die Standardsprache der App - nur ein zuvor gespeicherter
// Wechsel zu einer anderen Sprache (siehe LanguageSwitcherComponent) weicht davon ab.
const storedLang = typeof localStorage !== 'undefined' ? localStorage.getItem(LANGUAGE_STORAGE_KEY) : null;

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])), // Wichtig für den REST-API-Zugriff
    provideTranslateService({
      loader: provideTranslateHttpLoader({ prefix: '/i18n/', suffix: '.json' }),
      lang: storedLang ?? 'de',
      fallbackLang: 'de',
    }),
  ]
};
