package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.general.SharedDataEntity;
import de.naivetardis.landscaper.dto.net.HttpRequestEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    public ResponseEntity<?> forwardWithProxyService(HttpRequestEntity request, HttpServletResponse response) throws IOException {
        ResponseEntity<?> responseFromService = handlerClientFactory
                .apply(preparePortWhereServiceIsExposed(request))
                .method(request.getMethod())
                .uri(prepareURI(request))
                .headers(prepareHeadersFromOriginalRequest(request))
                .cookies(prepareCookiesFromOriginalRequest(request))
                .bodyValue(prepareBody(request.getBody()))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        addNewCookiesIntoServletResponse(request, response);
        addNewHeadersIntoServletResponse(responseFromService, response);

        log.info("Finalizing request for: {}", request.getRequestUri());

        return responseFromService;
    }

    private void addNewCookiesIntoServletResponse(HttpRequestEntity request, HttpServletResponse response) {
        for (Cookie cookie : request.getCookies()) {
            response.addCookie(cookie);
        }
    }

    private void addNewHeadersIntoServletResponse(ResponseEntity<?> responseFromService, HttpServletResponse response) {
        if(responseFromService.getHeaders() != null && !responseFromService.getHeaders().isEmpty()){
            responseFromService.getHeaders().forEach(new BiConsumer<String, List<String>>() {
                @Override
                public void accept(String s, List<String> strings) {
                    strings.forEach(new Consumer<String>() {
                        @Override
                        public void accept(String value) {
                            response.addHeader(s, value);
                        }
                    });
                }
            });
        }
    }

    private Object prepareBody(String body) {
        String result = body;
        if (!StringUtils.hasText(result)) {
            result = "";
        }
        return result;
    }

    private Consumer<MultiValueMap<String, String>> prepareCookiesFromOriginalRequest(HttpRequestEntity request) {
        return stringStringMultiValueMap -> request.getCookies().forEach(cookie -> stringStringMultiValueMap.add(cookie.getName(), cookie.getValue()));
    }

    private Consumer<HttpHeaders> prepareHeadersFromOriginalRequest(HttpRequestEntity request) {
        return httpHeaders -> request.getHeaders().forEach(httpHeaders::add);
    }

    private String preparePortWhereServiceIsExposed(HttpRequestEntity request) {
        String urlWhereServiceIsExposed = sharedData.getHostUrl();
        List<String> serverPath = List.of(request.getServerName().split("\\."));

        if (serverPath.contains("public")) {
            urlWhereServiceIsExposed += service.getRouteByKeyNames().get(serverPath.get(0) + "-" + serverPath.get(1));
        } else {
            urlWhereServiceIsExposed += service.getRouteByKeyNames().get(serverPath.get(0));
        }

        return urlWhereServiceIsExposed;
    }

    private String prepareURI(HttpRequestEntity request) {
        String uri = request.getRequestUri();

        if (StringUtils.hasText(request.getQueryString())) {
            uri += "?" + request.getQueryString();
        }

        return uri;
    }
}
