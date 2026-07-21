import { TestBed } from '@angular/core/testing';

import { FlightData } from './flight-data';

describe('FlightData', () => {
  let service: FlightData;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FlightData);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
