package de.naivetardis.landscaper.integration;

import de.naivetardis.landscaper.jobs.IpListener;
import de.naivetardis.landscaper.bridge.GoogleDynDNSBridge;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IpListenerUpdaterIntegrationTest extends BaseIntegration {

    @SpyBean
    private GoogleDynDNSBridge googleDynDNSBridge;

    @SpyBean
    private IpListener ipListener;

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateSuccessful() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.8")
                .addEndpoint("/googleDNS/nic/update?hostname=testhost&myip=0.0.0.8", 200)
                .build());

        await().untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNSBridge, times(1)).updateIpAddress(anyString()));

    }

    @Test
    public void givenIpListener_whenScheduledCall_thenListenerError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", 400)
                .addEndpoint("/googleDNS/nic/update?hostname=testhost&myip=0.0.0.8", 200)
                .build());

        ipListener.run();

        await().untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNSBridge, times(0)).updateIpAddress(anyString()));
    }

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.10")
                .addEndpoint("/googleDNS/nic/update?hostname=testhost&myip=0.0.0.10", 400)
                .build());

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> verify(ipListener, times(1)).run());
        await().untilAsserted(() -> verify(googleDynDNSBridge, times(3)).updateIpAddress(anyString()));
    }

}
