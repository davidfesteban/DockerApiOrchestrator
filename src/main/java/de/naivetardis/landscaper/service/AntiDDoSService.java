package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.general.SharedDataEntity;
import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.service.CachedMemoryService.MemoryType;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
@AllArgsConstructor
public class AntiDDoSService {
    private MemoryManagerService memoryManagerService;
    private SharedDataEntity sharedDataEntity;

    public void controlTries(HttpServletRequest request) {
        throwExceptionIfBanned(request);
        Map<String, Pair<Integer, ScheduledFuture<?>>> antiDDoS = memoryManagerService.viewFrom(MemoryType.ANTI_DDOS);

        if (antiDDoS.containsKey(request.getRemoteAddr())) {
            Pair<Integer, ScheduledFuture<?>> tries = antiDDoS.get(request.getRemoteAddr());
            memoryManagerService.editFrom(MemoryType.ANTI_DDOS, request.getRemoteAddr(), Pair.of(tries.getLeft() + 1, tries.getRight()));
        } else {
            memoryManagerService.editFrom(MemoryType.ANTI_DDOS, request.getRemoteAddr(),
                    Pair.of(0, memoryManagerService.newSchedulerDate(sharedDataEntity.getAuthTriesMinutes())));
        }

    }

    public void releaseTries(HttpServletRequest request) {
        memoryManagerService.removeFrom(MemoryType.ANTI_DDOS, request.getRemoteAddr());
    }

    private void throwExceptionIfBanned(HttpServletRequest request) {
        Optional.ofNullable((Pair<Integer, ScheduledFuture<?>>) memoryManagerService.viewFrom(MemoryType.ANTI_DDOS).get(request.getRemoteAddr()))
                .ifPresent(integerDatePair -> {
                    if (integerDatePair.getLeft() > sharedDataEntity.getAuthNumberTries()) {
                        throw new ConnectionException("You are banned!");
                    }
                });
    }
}
