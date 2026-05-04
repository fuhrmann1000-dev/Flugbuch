package de.windenshelter.flugbuch.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.windenshelter.flugbuch.model.StagingWinchkladdeEintrag;
import de.windenshelter.flugbuch.repository.WinchkladdeStagingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Skelett: tatsaechliche Implementierung folgt in Phase A
 * (siehe next-steps.md). Zweck dieser Klasse ist, dass die TDD-Tests
 * gegen eine konkrete Signatur laufen koennen.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WinchImportService {

    @SuppressWarnings("unused")
    private final WinchkladdeStagingRepository stagingRepository;

    public List<StagingWinchkladdeEintrag> importiereAusStream(InputStream csvInhalt) {
        throw new UnsupportedOperationException("TODO: CSV-Parser implementieren");
    }

    @Transactional
    public void importiereIdempotent(List<StagingWinchkladdeEintrag> eintraege) {
        throw new UnsupportedOperationException("TODO: idempotenter Upsert via existsByExternalId");
    }
}
