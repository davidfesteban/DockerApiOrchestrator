package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.utility.RandomString;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static de.naivetardis.landscaper.service.CachedMemoryService.MemoryType;

@Service
@AllArgsConstructor
@Slf4j
public class MemoryManagerService {
    private final RandomString randomString = new RandomString(30);
    private CachedMemoryService cachedMemoryService;
    private TaskScheduler taskScheduler;

    public String createRandomFor(String ip) {
        String random = randomString.nextString();

        editFrom(MemoryType.RANDOM_CREATED, ip, random);

        return random;
    }

    public <T> Map<String, T> viewFrom(MemoryType memoryType) {
        return (Map<String, T>) cachedMemoryService.syncViewFrom(memoryType, memoryType.getValue());
    }

    public <T> void editFrom(MemoryType memoryType, String key, T value) {
        cachedMemoryService.syncEditFrom(memoryType, key, value);
    }

    public <T> void removeFrom(MemoryType memoryType, String key) {
        cachedMemoryService.syncRemoveFrom(memoryType, key);
    }

    public ScheduledFuture<?> newSchedulerDate(Integer minutes) {
        final Date date = new Date();
        final Instant dateScheduled = date.toInstant().plus(minutes, ChronoUnit.MINUTES);
        log.info("Clean scheduled being {} on {}", date, dateScheduled);
        return taskScheduler.schedule(this::clearDataStored, dateScheduled);
    }

    private void clearDataStored() {
        log.info("Starting scheduled task");
        try {
            cachedMemoryService.syncRemoveOld();
        } catch (Exception e) {
            log.info("Unable to clear: {}", e.getMessage());
        }
        log.info("Finished scheduled task");
    }
}
