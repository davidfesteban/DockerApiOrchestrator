package de.naivetardis.landscaper.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class ProxyService {

    private Function<String, WebClient> handlerClientFactory;

    public ResponseEntity<?> forwardWithProxyService(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            uriCa = request.getRequestURI();
            if(StringUtils.hasText(request.getQueryString())) {
                uriCa += "?" + request.getQueryString();
            }
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
            ResponseEntity<?> response1 = handlerClientFactory.apply("http://localhost:1234").method(method)
                    .uri(uriCa)
                    .headers(new Consumer<HttpHeaders>() {
                        @Override
                        public void accept(HttpHeaders httpHeaders) {
                            request.getHeaderNames().asIterator().forEachRemaining(new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    httpHeaders.add(s, request.getHeader(s));
                                }
                            });
                        }
                    })
                    .cookies(new Consumer<MultiValueMap<String, String>>() {
                        @Override
                        public void accept(MultiValueMap<String, String> stringStringMultiValueMap) {
                            for (Cookie cookie : request.getCookies()) {
                                stringStringMultiValueMap.add(cookie.getName(), cookie.getValue());
                            }
                        }
                    })
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(byte[].class).block();

            response1.getHeaders().forEach(new BiConsumer<String, List<String>>() {
                @Override
                public void accept(String s, List<String> strings) {
                    response.addHeader(s, strings.get(0));
                }
            });

            log.info("Finalizing request for: {}", request.getRequestURI());

            return response1;

        } catch (HttpClientErrorException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsString());
        }
    }
}
