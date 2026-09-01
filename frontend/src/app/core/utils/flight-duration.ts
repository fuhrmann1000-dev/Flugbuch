/**
 * The backend only sends startTime/landingTime ("HH:mm" each) - there's no
 * precomputed duration field - so every place that needs a flight's length
 * (the logbook table, the dashboard's hour totals/chart) works it out from
 * those two strings via this shared helper.
 */
export function getDurationMinutes(startTime: string, landingTime: string): number {
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [landingHours, landingMinutes] = landingTime.split(':').map(Number);
    let totalMinutes = (landingHours * 60 + landingMinutes) - (startHours * 60 + startMinutes);
    if (totalMinutes < 0) {
        totalMinutes += 24 * 60; // landed after midnight
    }
    return totalMinutes;
}

export function getDurationHours(startTime: string, landingTime: string): number {
    return getDurationMinutes(startTime, landingTime) / 60;
}

export function formatDuration(startTime: string, landingTime: string): string {
    const totalMinutes = getDurationMinutes(startTime, landingTime);
    return `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m`;
}
