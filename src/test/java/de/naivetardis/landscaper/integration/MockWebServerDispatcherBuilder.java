package de.naivetardis.landscaper.integration;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class MockWebServerDispatcherBuilder {

    private final Map<String, MockResponse> endpoints;

    public MockWebServerDispatcherBuilder() {
        this.endpoints = new HashMap<>();
    }

    public static MockWebServerDispatcherBuilder builder() {
        return new MockWebServerDispatcherBuilder();
    }

    public MockWebServerDispatcherBuilder addEndpoint(String path) {
        addEndpoint(path, "");
        return this;
    }

    public MockWebServerDispatcherBuilder addEndpoint(String path, String body) {
        addEndpoint(path, body, 200);
        return this;
    }

    public MockWebServerDispatcherBuilder addEndpoint(String path, Integer responseCode) {
        addEndpoint(path, "", responseCode);
        return this;
    }

    public MockWebServerDispatcherBuilder addEndpoint(String path, String body, Integer responseCode) {
        addEndpoint(path, new MockResponse().setBody(body).setResponseCode(responseCode));
        return this;
    }

    public MockWebServerDispatcherBuilder addEndpoint(String path, MockResponse mockResponse) {
        endpoints.put(path, mockResponse);
        return this;
    }

    public Dispatcher build() {
        return new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                log.info("Request received on MockWebServer with body: {}; on path: {}, with headers: {}",
                        recordedRequest.getBody(),
                        recordedRequest.getPath(),
                        recordedRequest.getHeaders());
                return endpoints.get(recordedRequest.getPath());
            }
        };
    }


}
