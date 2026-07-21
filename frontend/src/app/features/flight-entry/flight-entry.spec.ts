import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlightEntry } from './flight-entry';

describe('FlightEntry', () => {
  let component: FlightEntry;
  let fixture: ComponentFixture<FlightEntry>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlightEntry]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlightEntry);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
