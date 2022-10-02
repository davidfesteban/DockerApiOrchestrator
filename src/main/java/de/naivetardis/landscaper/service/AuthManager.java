package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.utility.Default;
import de.naivetardis.landscaper.utility.RandomString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthManager {

    private final RandomString randomString = new RandomString(30);
    private final Map<String, Date> availableCodes = new HashMap<>();
    private final Map<String, Date> availableTokens = new HashMap<>();
    private final Map<String, Properties> waitingUsers = new HashMap<>();

    private final Map<String, Pair<Integer, Date>> antiDDoS = new HashMap<>();
    private final TaskScheduler taskScheduler;
    private WebClient handlerClient;
    @Value("${master.user}")
    private String user;
    @Value("${master.pass}")
    private String pass;

    public AuthManager(TaskScheduler taskScheduler, WebClient handlerClientBean) {
        this.taskScheduler = taskScheduler;
        this.handlerClient = handlerClientBean;
    }

    public static ResponseEntity<String> loginView() throws IOException {
        return new ResponseEntity<String>(Files.readString(Path.of("src/main/resources/web/login.html"), Charset.defaultCharset()), HttpStatus.OK);
    }

    public boolean isAuthenticated(HttpServletRequest request) {

        if (!hasCookies(request)) {
            return false;
        }

        return Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("proxyToken")).anyMatch(cookie -> availableTokens.keySet().stream().anyMatch(cookie.getValue()::equalsIgnoreCase));
    }

    public void storeWhileWaitingForAuth(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) {
        //TODO: Avoid crashing uuid
        String uuid = randomString.nextString();
        Properties properties = new Properties();
        properties.put("body", Default.of(body).orElse(""));
        properties.put("method", Default.of(method).orElse(HttpMethod.GET));
        properties.put("request", request);
        properties.put("date", newSchedulerDate());

        waitingUsers.put(uuid, properties);

        response.addCookie(new Cookie("proxyUUID", uuid));
    }

    public Date newSchedulerDate() {
        final Date date = new Date();
        final Instant dateScheduled = date.toInstant().plus(5, ChronoUnit.HOURS);
        taskScheduler.schedule(this::clearDataStored, dateScheduled);
        log.info("Clean scheduled being {} for {}", date, dateScheduled);
        return date;
    }

    public ResponseEntity<String> handleRequest(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return forwardWithProxyService(body, method, request, response);
    }

    public ResponseEntity<String> auth(String email, String pass, String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isValidAuth(email, pass)) {
            antiDDoSProtector(request, true);
            injectToken(response);

            if (StringUtils.hasText(code)) {
                availableCodes.put(code, newSchedulerDate());
            }

            return recoverStoredRequest(request, response);
        }

        antiDDoSProtector(request, false);
        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }

    public boolean isBanned(HttpServletRequest request) {
        return antiDDoS.entrySet().stream().filter(stringPairEntry -> stringPairEntry.getKey().equalsIgnoreCase(request.getRemoteAddr())).findFirst().filter(stringPairEntry -> stringPairEntry.getValue().getLeft() > 5).isPresent();

    }

    public ResponseEntity<String> authByOneTimeCode(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (availableCodes.containsKey(code)) {
            antiDDoSProtector(request, true);
            injectToken(response);
            return recoverStoredRequest(request, response);
        }

        antiDDoSProtector(request, false);
        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }

    public void clearCookies(HttpServletRequest request, HttpServletResponse response) {
        //TODO: Remove from memory of waitingUsers and authenticated
        clearCookie(request, "proxyUUID", response);
        clearCookie(request, "proxyToken", response);
    }

    private void antiDDoSProtector(HttpServletRequest request, boolean authenticated) {
        if (authenticated) {
            antiDDoS.remove(request.getRemoteAddr());
        } else {
            antiDDoS.computeIfPresent(request.getRemoteAddr(), (s, integerDatePair) -> Pair.of(integerDatePair.getLeft() + 1, integerDatePair.getRight()));
            antiDDoS.computeIfAbsent(request.getRemoteAddr(), s -> Pair.of(0, AuthManager.this.newSchedulerDate()));
        }
    }

    private boolean hasCookies(HttpServletRequest request) {
        return request.getCookies() != null && request.getCookies().length > 0;
    }

    private void clearDataStored() {
        log.info("Starting scheduled task");
        try {
            Date dateNow = new Date();
            availableCodes.forEach((s, date) -> {
                if (isPassedTime(dateNow).test(date)) {
                    availableCodes.remove(s);
                }
            });
            availableTokens.forEach((s, date) -> {
                if (isPassedTime(dateNow).test(date)) {
                    availableTokens.remove(s);
                }
            });
            waitingUsers.forEach((s, properties) -> {
                if (isPassedTime(dateNow).test((Date) properties.get("date"))) {
                    waitingUsers.remove(s);
                }
            });
            antiDDoS.forEach((s, integerDatePair) -> {
                if (isPassedTime(dateNow).test(integerDatePair.getRight())) {
                    antiDDoS.remove(s);
                }
            });
        } catch (Exception e) {
            log.info("Unable to clear: {}", e.getMessage());
        }
        log.info("Finished scheduled task");
    }

    private Predicate<Date> isPassedTime(Date dateNow) {
        return date -> dateNow.getTime() - date.getTime() / (1000 * 60 * 60.0) > 5;
    }

    private ResponseEntity<String> forwardWithProxyService(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        URI uri = null;
        try {
            uri = new URI("http", null, "www.google.es", 80, null, request.getQueryString(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if(!StringUtils.hasText(body)) {
            body = "";
        }
        String uriCa = "";

        if(!request.getRequestURI().equalsIgnoreCase("/auth"))  {
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
            return  handlerClient.method(method)
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

    private void injectToken(HttpServletResponse response) {
        String token = randomString.nextString();

        availableTokens.put(token, newSchedulerDate());
        response.addCookie(new Cookie("proxyToken", token));
    }

    private ResponseEntity<String> recoverStoredRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie cookieUUID = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("proxyUUID")).findFirst().get();
        Properties properties = waitingUsers.get(cookieUUID.getValue());

        clearCookie(request, cookieUUID.getName(), response);
        waitingUsers.remove(cookieUUID.getValue());

        return forwardWithProxyService((String) properties.get("body"), (HttpMethod) properties.get("method"), (HttpServletRequest) properties.get("request"), (HttpServletResponse) properties.get("response"));
    }

    private void clearCookie(HttpServletRequest request, String cookieName, HttpServletResponse response) {
        if (hasCookies(request)) {
            final Optional<Cookie> cookieCo = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase(cookieName))
                    .findFirst();

            if (cookieCo.isPresent()) {
                final Cookie cookie = cookieCo.get();
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }

        }
    }

    private boolean isValidAuth(String email, String pass) {
        if (StringUtils.hasText(email) && StringUtils.hasText(pass)) {
            return email.equalsIgnoreCase(this.user) && pass.equalsIgnoreCase(this.pass);
        }
        return false;
    }
}
