package de.naivetardis.landscaper.repository.impl;

import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.repository.interfaces.GoogleDynDNSRepository;
import de.naivetardis.landscaper.dto.GoogleDynDNSEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Component
public class GoogleDynDNS implements GoogleDynDNSRepository {

    private final GoogleDynDNSEntity googleDynDNSEntity;
    private final WebClient webClient;

    public GoogleDynDNS(WebClient googleApiBean) {
        this.googleDynDNSEntity = new GoogleDynDNSEntity();
        this.webClient = googleApiBean;
    }

    @Override
    public void updateIpAddress(String ip) {
        try{
            boolean result = Objects.requireNonNull(webClient.get()
                    .attribute("ip", ip)
                    .attribute("user", googleDynDNSEntity.getUser())
                    .attribute("pass", googleDynDNSEntity.getPass())
                    .retrieve().toBodilessEntity().block()).getStatusCode().is2xxSuccessful();

            if(!result) throw new ConnectionException();
        } catch (Exception e) {
            throw new ConnectionException();
        }
    }
}
