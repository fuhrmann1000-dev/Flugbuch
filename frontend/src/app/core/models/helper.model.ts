/** Matches the backend's CompetitionType enum. */
export type CompetitionType = 'HG' | 'PG';

/** Matches java.time.DayOfWeek's enum names, used as-is by the backend (see Helper#availableDays). */
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export const ALL_DAYS_OF_WEEK: DayOfWeek[] = [
    'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY',
];

/**
 * Body for POST /helpers/register - the single public create-or-update form.
 * Whether this creates a new helper or updates an existing one is decided by
 * the backend, based on whether `email` is already known.
 */
export interface HelperRegistrationRequest {
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    competition: CompetitionType | null;
    skills: string;
    availableDays: DayOfWeek[];
}

/** Reduced view from GET /helpers/public - no login required. */
export interface HelperPublicDto {
    firstName: string;
    competition: CompetitionType;
    skills: string;
    availableDays: DayOfWeek[];
}

/** Full record from GET /helpers - ADMIN only. */
export interface HelperAdminDto {
    id: number;
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    competition: CompetitionType;
    skills: string;
    availableDays: DayOfWeek[];
}
