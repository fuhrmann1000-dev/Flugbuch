package de.windenshelter.flugbuch.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.windenshelter.flugbuch.dto.FlugbuchEintragDto;
import de.windenshelter.flugbuch.service.FlightService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public List<FlugbuchEintragDto> findAll() {
        return flightService.findAll();
    }

    @GetMapping("/{id}")
    public FlugbuchEintragDto findById(@PathVariable Long id) {
        return flightService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FlugbuchEintragDto> create(@RequestBody FlugbuchEintragDto dto) {
        FlugbuchEintragDto created = flightService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public FlugbuchEintragDto update(@PathVariable Long id, @RequestBody FlugbuchEintragDto dto) {
        return flightService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
