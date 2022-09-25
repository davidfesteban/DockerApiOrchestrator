package de.naivetardis.landscaper.bridge;

import de.naivetardis.landscaper.annotation.Retryable;
import de.naivetardis.landscaper.dto.google.GoogleDynDNSEntity;
import de.naivetardis.landscaper.exception.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Component
@Slf4j
public class GoogleDynDNSBridge {

    private final GoogleDynDNSEntity googleDynDNSEntity;
    private final WebClient webClient;

    public GoogleDynDNSBridge(GoogleDynDNSEntity googleDynDNSEntity, WebClient googleApiBean) {
        this.googleDynDNSEntity = googleDynDNSEntity;
        this.webClient = googleApiBean;
    }

    @Retryable
    public void updateIpAddress(String ip) {
        try {
            boolean result = Objects.requireNonNull(webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/nic/update")
                            .queryParam("hostname", googleDynDNSEntity.getHostname())
                            .queryParam("myip", ip)
                            .build())
                    .retrieve().toBodilessEntity().block()).getStatusCode().is2xxSuccessful();
            log.info("Google update was {}", result);
            if (!result) throw new ConnectionException();
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }
}
