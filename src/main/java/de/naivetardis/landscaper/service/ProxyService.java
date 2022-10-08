package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.general.SharedDataEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class ProxyService {

    private DockerOrchestratorService service;
    private Function<String, WebClient> handlerClientFactory;
    private SharedDataEntity sharedData;

    public ResponseEntity<?> forwardWithProxyService(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseEntity<?> responseFromService = handlerClientFactory
                .apply(preparePortWhereServiceIsExposed(request))
                .method(method)
                .uri(prepareURI(request))
                .headers(prepareHeadersFromOriginalRequest(request))
                .cookies(prepareCookiesFromOriginalRequest(request))
                .bodyValue(prepareBody(body))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        addNewCookiesIntoServletResponse(request, response);
        addNewHeadersIntoServletResponse(responseFromService, response);

        log.info("Finalizing request for: {}", request.getRequestURI());

        return responseFromService;
    }

    private void addNewCookiesIntoServletResponse(HttpServletRequest request, HttpServletResponse response) {
        for (Cookie cookie : request.getCookies()) {
            response.addCookie(cookie);
        }
    }

    private void addNewHeadersIntoServletResponse(ResponseEntity<?> responseFromService, HttpServletResponse response) {
        Optional.of(responseFromService.getHeaders()).ifPresent(new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders httpHeaders) {
                if (!httpHeaders.isEmpty()) {
                    httpHeaders.forEach(new BiConsumer<String, List<String>>() {
                        @Override
                        public void accept(String s, List<String> strings) {
                            if (!strings.isEmpty() && response != null) {
                                response.addHeader(s, strings.get(0));
                            }
                        }
                    });
                }
                ;
            }
        });
    }

    private Object prepareBody(String body) {
        String result = body;
        if (!StringUtils.hasText(result)) {
            result = "";
        }
        return result;
    }

    private Consumer<MultiValueMap<String, String>> prepareCookiesFromOriginalRequest(HttpServletRequest request) {
        return cookieMemory -> Arrays.stream(
                        Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .forEach(cookie -> cookieMemory.add(cookie.getName(), cookie.getValue())
                );
    }

    private Consumer<HttpHeaders> prepareHeadersFromOriginalRequest(HttpServletRequest request) {
        return httpHeaders -> request.getHeaderNames().asIterator().forEachRemaining(name -> httpHeaders.add(name, request.getHeader(name)));
    }

    private String preparePortWhereServiceIsExposed(HttpServletRequest request) {
        String urlWhereServiceIsExposed = sharedData.getHostUrl();
        List<String> serverPath = List.of(request.getServerName().split("\\."));

        if (serverPath.size() > 3) {
            urlWhereServiceIsExposed += service.getRouteByKeyNames().get(serverPath.get(0) + "-" + serverPath.get(1));
        } else {
            urlWhereServiceIsExposed += service.getRouteByKeyNames().get(serverPath.get(0));
        }

        return urlWhereServiceIsExposed;
    }

    private String prepareURI(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (StringUtils.hasText(request.getQueryString())) {
            uri += "?" + request.getQueryString();
        }

        return uri;
    }
}
