package de.windenshelter.flugbuch.controller;

import de.windenshelter.flugbuch.dto.FlightLogEntryDto;
import de.windenshelter.flugbuch.dto.FlightSearchCriteria;
import de.windenshelter.flugbuch.dto.PageResponse;
import de.windenshelter.flugbuch.dto.SortableFlightField;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.windenshelter.flugbuch.service.FlightService;
import lombok.RequiredArgsConstructor;

/** REST endpoints for flight log entries: list (filter/sort/paginate), get one, create, update, delete. */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    /**
     * Returns flight log entries, paginated and sorted, optionally narrowed
     * down by any combination of filter query params, e.g.:
     * {@code ?pilot=Max}, {@code ?date=29.07.2026} (today),
     * {@code ?dateFrom=01.07.2026&dateTo=31.07.2026}, {@code ?aircraftType=ASK 21}.
     * Fields left out of the query string are simply not filtered on.
     *
     * Pagination: {@code ?page=0&size=20}. Sorting is by a single field at a
     * time via two dropdowns in Swagger: {@code sortBy} (one of
     * {@link SortableFlightField}) and {@code sortDirection}
     * ({@code ASC}/{@code DESC}), e.g. {@code ?sortBy=DATE&sortDirection=DESC}.
     * Leaving {@code sortBy} empty means "no particular order".
     */
    @GetMapping
    public PageResponse<FlightLogEntryDto> findAll(@ParameterObject @ModelAttribute FlightSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SortableFlightField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection) {
        Page<FlightLogEntryDto> result = flightService.findAll(criteria, toPageable(page, size, sortBy, sortDirection));
        return PageResponse.of(result);
    }

    /** Builds a Pageable from the plain page/size/sortBy/sortDirection params (unsorted if sortBy is null). */
    private Pageable toPageable(int page, int size, SortableFlightField sortBy, Sort.Direction sortDirection) {
        if (sortBy == null) {
            return PageRequest.of(page, size);
        }
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy.getDtoFieldName()));
    }

    @GetMapping("/{id}")
    public FlightLogEntryDto findById(@PathVariable Long id) {
        return flightService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FlightLogEntryDto> create(@RequestBody FlightLogEntryDto dto) {
        FlightLogEntryDto created = flightService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public FlightLogEntryDto update(@PathVariable Long id, @RequestBody FlightLogEntryDto dto) {
        return flightService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
