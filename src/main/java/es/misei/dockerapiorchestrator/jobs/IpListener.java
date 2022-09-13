package es.misei.dockerapiorchestrator.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class IpListener {

    private final WebClient ipifyApiBean;

    public IpListener(WebClient ipifyApiBean) {
        this.ipifyApiBean = ipifyApiBean;
    }

    @Scheduled(fixedRateString = "${ipify-listener.minutes}", timeUnit = TimeUnit.MINUTES)
    public void run() {
        try {
            Objects.requireNonNull(ipifyApiBean.get().retrieve().toEntity(String.class).block()).getBody();
        } catch (Exception e) {
            log.info("Unable to retrieve ip because: {}", e.getMessage());
        }
    }


}
