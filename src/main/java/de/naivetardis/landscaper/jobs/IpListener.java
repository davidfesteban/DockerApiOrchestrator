package de.naivetardis.landscaper.jobs;

import de.naivetardis.landscaper.service.IpUpdater;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final IpUpdater ipUpdater;

    @Scheduled(initialDelay = 5, fixedDelayString = "${ipify-listener.seconds}", timeUnit = TimeUnit.SECONDS)
    public void run() {
        try {
            ipUpdater.updateIpOnGoogleDynDns(Objects.requireNonNull(ipifyApiBean.get().retrieve().toEntity(String.class).block()).getBody());
        } catch (Exception e) {
            log.info("Unable to retrieve ip because: {}", e.getMessage());
        }
    }


}
