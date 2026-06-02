export interface FlightLogEntry {
    id: string;
    flightDate: string;               // Format: "DD.MM.YYYY"
    startTimeUtc: string;             // Format: "HH:MM"
    landingTimeUtc: string;           // Format: "HH:MM"
    durationFormatted: string;        // z.B. "1h 13m" (wird berechnet)
    aircraftRegistration: string;     // z.B. "D-MVBO"
    aircraftModel: string;            // z.B. "Merlin 1200"
    pilotInCommandName: string;       // Name des verantwortlichen Piloten
    departureLocation: string;        // "SLP Altes Lager"
    arrivalLocation: string;          // "SLP Altes Lager"
    flightType: string;               // "Schlepp", "Charter VFR", "Instruction"
    numberOfLandings: number;         // Landungen
    remarks?: string;                 // Bemerkungen / expanding Details
}