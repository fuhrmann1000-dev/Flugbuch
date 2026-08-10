/** localStorage key under which the pilot's chosen UI language ('de' | 'en') is persisted. */
export const LANGUAGE_STORAGE_KEY = 'flugbuch_lang';

/** Languages the UI can be switched to; German is the default/source language. */
export const SUPPORTED_LANGUAGES = ['de', 'en'] as const;
export type SupportedLanguage = typeof SUPPORTED_LANGUAGES[number];
