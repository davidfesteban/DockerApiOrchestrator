package de.naivetardis.landscaper.configuration;

import de.naivetardis.landscaper.dto.google.GoogleDynDNSEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${ipify.url}")
    private String ipifyApi;

    @Value("${google-dyn-dns.url}")
    private String googleApi;

    @Value("${handler.codec.mb-memory-size}")
    private Integer memorySize;

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

    @Bean
    public Function<String, WebClient> handlerClientFactory() {
        final int size = memorySize * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        return url -> WebClient.builder()
                .baseUrl(url)
                .exchangeStrategies(strategies)
                .build();
    }

}
