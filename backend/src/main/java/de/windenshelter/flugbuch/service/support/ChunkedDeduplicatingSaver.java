package de.windenshelter.flugbuch.service.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Encapsulates the recurring pattern "deduplicate against a key,
 * process in chunks, query existing keys, save only new items".
 * K is generic so that both externalId (integer) and a natural key
 * (e.g., date + start time + identifier) fit.
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

    public record Result(int stored, int skipped) {
    }

    public Result saveIdempotent(List<T> entries, Consumer<List<T>> saveAll) {
        Map<K, T> uniqueByKey = new LinkedHashMap<>();
        for (T entry : entries) {
            uniqueByKey.putIfAbsent(keyExtractor.apply(entry), entry);
        }

        int saved = 0;
        int skipped = entries.size() - uniqueByKey.size();

        List<T> unique = new ArrayList<>(uniqueByKey.values());
        for (int start = 0; start < unique.size(); start += chunkSize) {
            List<T> chunk = unique.subList(start, Math.min(start + chunkSize, unique.size()));

            Set<K> known = existingKeysLookup.apply(chunk);
            List<T> newList = chunk.stream()
                    .filter(e -> !known.contains(keyExtractor.apply(e)))
                    .toList();

            saveAll.accept(newList);
            saved += newList.size();
            skipped += chunk.size() - newList.size();
        }

        return new Result(saved, skipped);
    }
}