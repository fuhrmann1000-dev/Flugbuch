import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { FlightDataService } from '../../core/services/flight-data';
import { FlightLogEntry } from '../../core/models/flight-log-entry.model';

@Component({
  selector: 'app-flight-entry',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  public get pageTitle(): string {
    return this.isEditMode() ? 'Flugdaten bearbeiten' : 'Neuer Flugeintrag';
  }

  public save(): void {
    this.isSaving.set(true);
    this.saveSuccess.set(false);
    this.saveError.set(false);

    const entry: Partial<FlightLogEntry> = {
      flightDate: this.flightDate,
      startTimeUtc: this.startTimeUtc,
      landingTimeUtc: this.landingTimeUtc,
      aircraftModel: this.aircraftModel,
      aircraftRegistration: this.aircraftRegistration,
      pilotInCommandName: this.pilotInCommandName,
      numberOfLandings: this.numberOfLandings,
      departureLocation: this.departureLocation,
      arrivalLocation: this.arrivalLocation,
      flightType: this.flightType,
      remarks: this.remarks,
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
}