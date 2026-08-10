/** localStorage keys for the pilot's app preferences (Settings page). */
export const DATE_FORMAT_STORAGE_KEY = 'flugbuch_date_format';
export const PAGE_SIZE_STORAGE_KEY = 'flugbuch_default_page_size';

export const DATE_FORMATS = ['dd.MM.yyyy', 'MM/dd/yyyy'] as const;
export type DateFormat = typeof DATE_FORMATS[number];

export const PAGE_SIZE_OPTIONS = [10, 25, 50] as const;
export type PageSizeOption = typeof PAGE_SIZE_OPTIONS[number];

export const DEFAULT_DATE_FORMAT: DateFormat = 'dd.MM.yyyy';
export const DEFAULT_PAGE_SIZE: PageSizeOption = 10;
