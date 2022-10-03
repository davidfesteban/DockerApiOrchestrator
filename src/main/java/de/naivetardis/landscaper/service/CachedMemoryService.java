package de.naivetardis.landscaper.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class CachedMemoryService {

    private final Map<String, Map<String, ?>> cachedMemories = new HashMap<>();

    //TODO: Improve this. Unchecked
    public synchronized <T> Map<String, T> syncViewFrom(MemoryType memoryType, T clazz) {
        return (Map<String, T>) ImmutableMap.copyOf(cachedMemories.get(memoryType.name()));
    }

    public synchronized <T> void syncEditFrom(MemoryType memoryType, String key, T value) {
        //TODO: Improve this. Generify computeWhenever
        ((Map<String, T>) cachedMemories.get(memoryType.name())).computeIfPresent(key, (s, oldValue) -> value);
        ((Map<String, T>) cachedMemories.get(memoryType.name())).computeIfAbsent(key, (s) -> value);
    }

    public synchronized void syncRemoveFrom(MemoryType memoryType, String key) {
        memoryType.removeFromByBiConsumer().accept(key, cachedMemories.get(memoryType.name()));
    }

    //TODO: Improve this shit
    public synchronized void syncRemoveOld() {
        Map<String, MemoryType> oldEntries = new HashMap<>();

        for (MemoryType value : MemoryType.values()) {
            cachedMemories.get(value).entrySet().forEach(new Consumer<Map.Entry<String, ?>>() {
                @Override
                public void accept(Map.Entry<String, ?> stringEntry) {
                    if(stringEntry.getValue() instanceof ScheduledFuture &&
                            ((ScheduledFuture) stringEntry.getValue()).isDone()) {
                        cachedMemories.get(value).remove(stringEntry.getKey());
                    }
                }
            });
        }
    }

    @PostConstruct
    private void initializeCache() {
        Arrays.stream(MemoryType.class.getEnumConstants())
                .forEach(memoryType -> cachedMemories.put(memoryType.name(), new HashMap<>()));
    }

    public enum MemoryType {
        //Key: IP, Value: RandomString
        RANDOM_CREATED(String.class, (BiConsumer<String, Map<String, String>>) (s, stringStringMap) -> {
            stringStringMap.remove(s);
        }),
        //Key: RandomString, Value: Date
        AVAILABLE_CODES(ScheduledFuture.class, (BiConsumer<String, Map<String, ScheduledFuture<?>>>) (s, map) -> {
            if (map.containsKey(s)) {
                map.get(s).cancel(true);
                map.remove(s);
            }
        }),
        //Key: RandomString, Value: Date
        AVAILABLE_TOKENS(ScheduledFuture.class, (BiConsumer<String, Map<String, ScheduledFuture<?>>>) (s, map) -> {
            if (map.containsKey(s)) {
                map.get(s).cancel(true);
                map.remove(s);
            }
        }),
        //Key: RandomString, Value: Properties
        WAITING_USERS(Properties.class, (BiConsumer<String, Map<String, Properties>>) (s, map) -> {
            if (map.containsKey(s)) {
                ((ScheduledFuture<?>) map.get(s).get("date")).cancel(true);
                map.remove(s);
            }
        }),
        //Key: IP, Value: Tries, ScheduledFuture
        ANTI_DDOS(Pair.class, (BiConsumer<String, Map<String, Pair<Integer, ScheduledFuture<?>>>>) (s, map) -> {
            if (map.containsKey(s)) {
                map.get(s).getRight().cancel(true);
                map.remove(s);
            }
        });

        private final Class<?> value;
        private final RemoveSteps removeSteps;

        // private enum constructor
        private <T> MemoryType(Class<?> value, BiConsumer<String, Map<String, T>> removeSteps) {
            this.value = value;
            this.removeSteps = new RemoveSteps<>(removeSteps);
        }

        public Class<?> getValue() {
            return value;
        }

        public BiConsumer<String, Map<String, ?>> removeFromByBiConsumer() {
            return removeSteps.removeSteps;
        }

        private static class RemoveSteps<T> {
            BiConsumer<String, Map<String, T>> removeSteps;

            public RemoveSteps(BiConsumer<String, Map<String, T>> removeSteps) {
                this.removeSteps = removeSteps;
            }
        }

    }


}
