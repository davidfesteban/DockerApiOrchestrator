package de.naivetardis.landscaper.integration;

import de.naivetardis.landscaper.jobs.IpListener;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.retry.annotation.EnableRetry;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class IpListenerUpdaterIntegrationTest extends BaseIntegration {

    private IpListener ipListener;

    @Autowired
    public IpListenerUpdaterIntegrationTest(IpListener ipListener) {
        this.ipListener = ipListener;
    }

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateSuccessful() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.8")
                .addEndpoint("/googleDNS", 200)
                .build());

        ipListener.run();

        //TODO: Assert messages
    }

    @Test
    public void givenIpListener_whenScheduledCall_thenListenerError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", 400)
                .addEndpoint("/googleDNS", 200)
                .build());

        ipListener.run();

        //TODO: Assert messages of exception
    }

    @Test
    public void givenIpListener_whenScheduledCall_andNewIp_thenUpdateError() {
        BaseIntegration.mockBackEnd.setDispatcher(MockWebServerDispatcherBuilder.builder()
                .addEndpoint("/ipListener", "0.0.0.8")
                .addEndpoint("/googleDNS", 400)
                .build());

        ipListener.run();

        //TODO: Assert messages of exception
    }


}
