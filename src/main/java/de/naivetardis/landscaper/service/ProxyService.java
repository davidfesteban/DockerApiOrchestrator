package de.naivetardis.landscaper.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class ProxyService {

    private Function<String, WebClient> handlerClientFactory;

    public ResponseEntity<String> forwardWithProxyService(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        URI uri = null;
        try {
            uri = new URI("http", null, "www.google.es", 80, null, request.getQueryString(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (!StringUtils.hasText(body)) {
            body = "";
        }
        String uriCa = "";

        if (!request.getRequestURI().equalsIgnoreCase("/auth")) {
            uriCa = request.getRequestURI() + "?" + request.getQueryString();
        }

        /*headers(new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders httpHeaders) {
                httpHeaders.addAll(Collections.list(request.getHeaderNames())
                        .stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                h -> Collections.list(request.getHeaders(h)),
                                (oldValue, newValue) -> newValue,
                                HttpHeaders::new
                        )));
            }
        })*/

        try {
            return handlerClientFactory.apply("http://www.google.es").method(method)
                    .uri(uriCa)
                    .bodyValue(body)
                    .retrieve().toEntity(String.class).block();

        } catch (HttpClientErrorException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsString());
        }
    }
}
