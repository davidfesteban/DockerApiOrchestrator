package de.naivetardis.landscaper.jobs;

import de.naivetardis.landscaper.service.IpUpdaterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class IpListener {

    private final WebClient ipifyApiBean;
    private final IpUpdaterService ipUpdaterService;

    @Scheduled(initialDelay = 5, fixedRateString = "${ipify-listener.seconds}", timeUnit = TimeUnit.SECONDS)
    public void run() {
        log.info("Starting scheduled task");
        try {
            ipUpdaterService.updateIpOnGoogleDynDns(Objects.requireNonNull(ipifyApiBean.get().retrieve().toEntity(String.class).block()).getBody());
        } catch (Exception e) {
            log.info("Unable to retrieve ip because: {}", e.getMessage());
        }
        log.info("Finished scheduled task");
    }


}
