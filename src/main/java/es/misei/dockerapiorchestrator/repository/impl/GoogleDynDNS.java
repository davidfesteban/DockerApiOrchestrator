package es.misei.dockerapiorchestrator.repository.impl;

import es.misei.dockerapiorchestrator.dto.GoogleDynDNSEntity;
import es.misei.dockerapiorchestrator.repository.interfaces.GoogleDynDNSRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Component
public class GoogleDynDNS implements GoogleDynDNSRepository {

    private final GoogleDynDNSEntity googleDynDNSEntity;
    private final WebClient webClient;

    public GoogleDynDNS() {
        this.googleDynDNSEntity = new GoogleDynDNSEntity();
        this.webClient = WebClient.create(googleDynDNSEntity.getBaseUrl());
    }


    @Override
    public Boolean updateIpAddress(String ip) {
        return Objects.requireNonNull(webClient.get()
                .attribute("ip", ip)
                .attribute("user", googleDynDNSEntity.getUser())
                .attribute("pass", googleDynDNSEntity.getPass())
                .retrieve().toBodilessEntity().block()).getStatusCode().is2xxSuccessful();
    }
}
