/**
 * Mirrors the backend's FlightLogEntryDto field-for-field. Date/time fields
 * come over the wire as strings ("dd.MM.yyyy" / "HH:mm"), not Date objects -
 * that's how the backend serializes LocalDate/LocalTime.
 */
export interface FlightLogEntry {
    id: number;
    date: string;                     // Format: "DD.MM.YYYY"
    startTime: string;                // Format: "HH:MM"
    landingTime: string;               // Format: "HH:MM"
    aircraftType: string;
    registration: string;
    pilot: string;
    guests: number | null;
    flightType: string;
    departureAirfield: string;
    destinationAirfield: string;
    flightDirector: string | null;
    towedAircraft: string | null;
    towHeight: number | null;
    amount: number | null;
    remarks: string | null;
    flightCount: number | null;
}
