/** Parses the backend's "dd.MM.yyyy" date strings into a real Date, for local date-math (month/week bucketing). */
export function parseDdMmYyyy(date: string): Date {
    const [day, month, year] = date.split('.').map(Number);
    return new Date(year, month - 1, day);
}

/** Monday of the week {@code date} falls in, at midnight - used to bucket "this week" stats. */
export function startOfWeek(date: Date): Date {
    const result = new Date(date);
    const day = result.getDay(); // 0 = Sunday
    const diffToMonday = day === 0 ? -6 : 1 - day;
    result.setDate(result.getDate() + diffToMonday);
    result.setHours(0, 0, 0, 0);
    return result;
}
