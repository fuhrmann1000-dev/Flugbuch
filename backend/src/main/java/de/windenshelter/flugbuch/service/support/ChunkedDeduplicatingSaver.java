package de.windenshelter.flugbuch.service.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Kapselt das wiederkehrende Muster "dedupliziere gegen einen Schluessel,
 * verarbeite in Chunks, frage vorhandene Schluessel ab, speichere nur Neues".
 * K ist generisch, damit sowohl externalId (Integer) als auch ein
 * natuerlicher Schluessel (z.B. Datum+Startzeit+Kennzeichen) passen.
 */
public final class ChunkedDeduplicatingSaver<T, K> {

    private final int chunkSize;
    private final Function<T, K> keyExtractor;
    private final Function<List<T>, Set<K>> existingKeysLookup;

    public ChunkedDeduplicatingSaver(int chunkSize, Function<T, K> keyExtractor,
                                     Function<List<T>, Set<K>> existingKeysLookup) {
        this.chunkSize = chunkSize;
        this.keyExtractor = keyExtractor;
        this.existingKeysLookup = existingKeysLookup;
    }

    public record Ergebnis(int gespeichert, int uebersprungen) {
    }

    public Ergebnis speichereIdempotent(List<T> eintraege, Consumer<List<T>> saveAll) {
        Map<K, T> eindeutigeNachSchluessel = new LinkedHashMap<>();
        for (T eintrag : eintraege) {
            eindeutigeNachSchluessel.putIfAbsent(keyExtractor.apply(eintrag), eintrag);
        }

        int gespeichert = 0;
        int uebersprungen = eintraege.size() - eindeutigeNachSchluessel.size();

        List<T> eindeutige = new ArrayList<>(eindeutigeNachSchluessel.values());
        for (int start = 0; start < eindeutige.size(); start += chunkSize) {
            List<T> chunk = eindeutige.subList(start, Math.min(start + chunkSize, eindeutige.size()));

            Set<K> bekannt = existingKeysLookup.apply(chunk);
            List<T> neu = chunk.stream()
                    .filter(e -> !bekannt.contains(keyExtractor.apply(e)))
                    .toList();

            saveAll.accept(neu);
            gespeichert += neu.size();
            uebersprungen += chunk.size() - neu.size();
        }

        return new Ergebnis(gespeichert, uebersprungen);
    }
}