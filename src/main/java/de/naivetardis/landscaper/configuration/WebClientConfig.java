package de.naivetardis.landscaper.configuration;

import de.naivetardis.landscaper.dto.GoogleDynDNSEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${ipify.url}")
    private String ipifyApi;

    @Value("${google-dyn-dns.url}")
    private String googleApi;

    @Bean
    public WebClient ipifyApiBean() {
        log.info("IpifyApi url {}", ipifyApi);
        return WebClient.create(ipifyApi);
    }

    @Bean
    public WebClient googleApiBean(GoogleDynDNSEntity googleDynDNSEntity) {
        log.info("Google url {}", googleApi);
        return WebClient.builder()
                .defaultHeader("Authorization", googleDynDNSEntity.getAuth())
                .defaultHeader("User-Agent", "Chrome/41.0")
                .baseUrl(googleApi).build();
    }

}
