package es.misei.dockerapiorchestrator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ipify.url}")
    private String ipifyApi;

    @Value("${google-dyn-dns.url}")
    private String googleApi;

    @Bean
    public WebClient ipifyApiBean() {
        return WebClient.create(ipifyApi);
    }
    @Bean
    public WebClient googleApiBean() {
        return WebClient.create(googleApi);
    }
}
