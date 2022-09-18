package de.naivetardis.landscaper.integration;

import de.naivetardis.landscaper.jobs.IpListener;
import de.naivetardis.landscaper.repository.impl.GoogleDynDNS;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

public class IpListenerUpdaterIntegrationTest extends BaseIntegration {

    @SpyBean(name = "googleApiBean")
    private WebClient googleApiBean;

    @SpyBean
    private GoogleDynDNS googleDynDNS;

    @SpyBean
    private IpListener ipListener;

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateSuccessful() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.8")
                .addEndpoint("/googleDNS", 200)
                .build());

        await().untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNS, times(1)).updateIpAddress(anyString()));

    }

    @Test
    public void givenIpListener_whenScheduledCall_thenListenerError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", 400)
                .addEndpoint("/googleDNS", 200)
                .build());

        ipListener.run();

        await().untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNS, times(0)).updateIpAddress(anyString()));
    }

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.8")
                .addEndpoint("/googleDNS", 400)
                .build());

        await().untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNS, times(3)).updateIpAddress(anyString()));
    }


}
