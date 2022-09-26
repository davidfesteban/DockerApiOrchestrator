package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.utility.RandomString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Service
@Slf4j
public class AuthManager {

    private final RandomString randomString = new RandomString(30);
    private final Map<String, Date> availableCodes = new HashMap<>();
    private final Map<String, Date> availableTokens = new HashMap<>();
    private final Map<String, Properties> waitingUsers = new HashMap<>();
    @Value("${master.user}")
    private String user;
    @Value("${master.pass}")
    private String pass;

    public static ResponseEntity<String> loginView() throws IOException {
        return new ResponseEntity<String>(Files.readString(Path.of("src/main/resources/web/login.html"), Charset.defaultCharset()), HttpStatus.OK);
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("proxyToken"))
                .anyMatch(cookie -> availableTokens.keySet().stream().anyMatch(cookie.getValue()::equalsIgnoreCase));
    }

    public void storeWhileWaitingForAuth(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) {
        String uuid = randomString.nextString();
        Properties properties = new Properties();
        properties.put("body", body);
        properties.put("method", method);
        properties.put("request", request);
        properties.put("date", new Date());

        waitingUsers.put(uuid, properties);

        response.addCookie(new Cookie("proxyUUID", uuid));
    }

    public ResponseEntity<String> handleRequest(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return forwardWithProxyService(body, method, request, response);
    }

    public ResponseEntity<String> auth(String email, String pass, String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isValidAuth(email, pass)) {
            injectToken(response);

            if (StringUtils.hasText(code)) {
                availableCodes.put(code, new Date());
            }

            return recoverStoredRequest(request);
        }

        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }

    public ResponseEntity<String> authByOneTimeCode(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (availableCodes.containsKey(code)) {
            injectToken(response);
            return recoverStoredRequest(request);
        }

        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }

    private void injectToken(HttpServletResponse response) {
        String token = randomString.nextString();

        availableTokens.put(token, new Date());
        response.addCookie(new Cookie("proxyToken", token));
    }

    public void clearCookies(HttpServletRequest request) {
        clearCookie(request, "proxyUUID");
        clearCookie(request, "proxyToken");
    }

    private void clearCookie(HttpServletRequest request, String cookieName) {
        Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase(cookieName))
                .forEach(cookie -> cookie.setMaxAge(0));
    }

    private boolean isValidAuth(String email, String pass) {
        if (StringUtils.hasText(email) && StringUtils.hasText(pass)) {
            return email.equalsIgnoreCase(this.user) && pass.equalsIgnoreCase(this.pass);
        }
        return false;
    }

    private ResponseEntity<String> recoverStoredRequest(HttpServletRequest request) throws IOException {
        Cookie cookieUUID = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("proxyUUID")).findFirst().get();
        Properties properties = waitingUsers.get(cookieUUID.getValue());

        clearCookie(request, cookieUUID.getName());
        waitingUsers.remove(cookieUUID.getValue());

        return forwardWithProxyService((String) properties.get("body"), (HttpMethod) properties.get("method"), (HttpServletRequest) properties.get("request")
                , (HttpServletResponse) properties.get("response"));
    }

    private ResponseEntity<String> forwardWithProxyService(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TODO: Call service with proxyService on return
        return loginView();
    }

    @Scheduled(initialDelay = 5, fixedRate = 2, timeUnit = TimeUnit.HOURS)
    public void clearDataStored() {
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
        } catch (Exception e) {
            log.info("Unable to clear: {}", e.getMessage());
        }
        log.info("Finished scheduled task");
    }

    private Predicate<Date> isPassedTime(Date dateNow) {
        return date -> dateNow.getTime() - date.getTime() / (1000 * 60 * 60.0) > 5;
    }
}
