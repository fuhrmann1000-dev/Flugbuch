/** Maps a flight's free-text flightType (e.g. "Schlepp", "Charter VFR") to one of the app's fixed badge colors. */
export function getFlightTypeBadgeClass(flightType: string): string {
    switch (flightType?.toLowerCase()) {
        case 'towing':
        case 'schlepp':
            return 'badge-towing';
        case 'charter vfr':
        case 'vfr charter':
            return 'badge-charter';
        case 'instruction':
            return 'badge-instruction';
        default:
            return 'badge-default';
    }
}

/** Same "is this a tow" rule the badge mapping uses, for counting/aggregation (e.g. the dashboard's tow-flights stat). */
export function isTowFlight(flightType: string): boolean {
    return getFlightTypeBadgeClass(flightType) === 'badge-towing';
}
