import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { FlightDataService } from '../../core/services/flight-data';
import { FlightLogEntry } from '../../core/models/flight-log-entry.model';

@Component({
  selector: 'app-flight-entry',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './flight-entry.html',
  styleUrls: ['./flight-entry.scss'],
})
export class FlightEntryComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly flightDataService = inject(FlightDataService);

  public readonly isEditMode = signal<boolean>(false);
  public readonly isSaving = signal<boolean>(false);
  public readonly saveSuccess = signal<boolean>(false);
  public readonly saveError = signal<boolean>(false);

  public readonly flightTypeOptions: string[] = [
    'Schlepp', 'Schlepp DoSi', 'Charter VFR', 'Instruction',
    'VFR Charter', 'Towing', 'Uebungsflug', 'Passagierflug',
  ];

  public flightDate: string = '';
  public startTimeUtc: string = '';
  public landingTimeUtc: string = '';
  public aircraftModel: string = '';
  public aircraftRegistration: string = '';
  public pilotInCommandName: string = '';
  public numberOfLandings: number = 1;
  public departureLocation: string = '';
  public arrivalLocation: string = '';
  public flightType: string = '';
  public remarks: string = '';
  public guests: number = 0;
  public flightController: string = '';
  public towedAircraft: string = '';
  public towHeight: string = '';

  public ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
    }
  }

  /** Translation key for the page/card title - resolved via the translate pipe in the template. */
  public get pageTitle(): string {
    return this.isEditMode() ? 'FLIGHT_ENTRY.EDIT_TITLE' : 'FLIGHT_ENTRY.NEW_TITLE';
  }

  public save(): void {
    this.isSaving.set(true);
    this.saveSuccess.set(false);
    this.saveError.set(false);

    // Maps this form's local field names to the backend's FlightLogEntryDto
    // field names (see core/models/flight-log-entry.model.ts).
    const entry: Partial<FlightLogEntry> = {
      date: this.toBackendDateFormat(this.flightDate),
      startTime: this.startTimeUtc,
      landingTime: this.landingTimeUtc,
      aircraftType: this.aircraftModel,
      registration: this.aircraftRegistration,
      pilot: this.pilotInCommandName,
      flightCount: this.numberOfLandings,
      departureAirfield: this.departureLocation,
      destinationAirfield: this.arrivalLocation,
      flightType: this.flightType,
      remarks: this.remarks,
      guests: this.guests,
      flightDirector: this.flightController,
      towedAircraft: this.towedAircraft,
      towHeight: this.towHeight ? Number(this.towHeight) : null,
    };

    this.flightDataService.createFlightLogEntry(entry).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.saveSuccess.set(true);
        setTimeout(() => this.router.navigate(['/flights']), 1200);
      },
      error: () => {
        this.isSaving.set(false);
        this.saveError.set(true);
      },
    });
  }

  public cancel(): void {
    this.router.navigate(['/flights']);
  }

  /**
   * The native <input type="date"> gives us "YYYY-MM-DD" (ISO), but the
   * backend's FlightLogEntryDto expects "dd.MM.yyyy" - without this
   * conversion, saving fails with a 400.
   */
  private toBackendDateFormat(isoDate: string): string {
    const [year, month, day] = isoDate.split('-');
    return `${day}.${month}.${year}`;
  }
}
