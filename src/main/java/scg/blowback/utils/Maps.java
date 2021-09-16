package scg.blowback.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static scg.blowback.utils.Predicates.at;

import static java.util.stream.Collectors.toUnmodifiableMap;

public final class Maps {

    private Maps() {
        throw new UnsupportedOperationException();
    }

    public static <K, V> Map<K, V> filterValues(Map<K, V> map, Predicate<V> filter) {
        return map.entrySet()
                .stream()
                .filter(at(Entry::getValue, filter))
                .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

}
