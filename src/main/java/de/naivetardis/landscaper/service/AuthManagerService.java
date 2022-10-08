package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.general.SharedDataEntity;
import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.service.CachedMemoryService.MemoryType;
import de.naivetardis.landscaper.utility.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import static de.naivetardis.landscaper.service.CachedMemoryService.MemoryType.AVAILABLE_TOKENS;
import static de.naivetardis.landscaper.service.CachedMemoryService.MemoryType.WAITING_USERS;
import static de.naivetardis.landscaper.utility.AuthUtils.*;
import static de.naivetardis.landscaper.utility.AuthUtils.CookieType.PROXY_TOKEN_NAME;
import static de.naivetardis.landscaper.utility.AuthUtils.CookieType.PROXY_UUID_NAME;

@Service
@AllArgsConstructor
@Slf4j
public class AuthManagerService {
    private MemoryManagerService memoryManagerService;
    private ProxyService proxyService;
    private SharedDataEntity sharedDataEntity;

    public boolean isTokenPresent(HttpServletRequest request) {
        if (!hasCookies(request)) {
            return false;
        }

        final Set<String> availableTokens = memoryManagerService.viewFrom(AVAILABLE_TOKENS).keySet();

        return Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase(PROXY_TOKEN_NAME.name()))
                .anyMatch(cookie -> availableTokens.stream().anyMatch(cookie.getValue()::equalsIgnoreCase));
    }


    public void storeWhileWaitingForAuth(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) {
        memoryManagerService.editFrom(MemoryType.WAITING_USERS,
                injectCookie(request, response, PROXY_UUID_NAME),
                AuthUtils.requestToProperties(body, method, request,
                        memoryManagerService.newSchedulerDate(sharedDataEntity.getWaitingUserMinutes())));

    }

    public ResponseEntity<?> handleRequest(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return proxyService.forwardWithProxyService(body, method, request, response);
    }

    public ResponseEntity<?> auth(String email, String pass, String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isValidAuth(email, pass, sharedDataEntity.getUser(), sharedDataEntity.getPass())) {

            memoryManagerService.editFrom(AVAILABLE_TOKENS,
                    injectCookie(request, response, PROXY_TOKEN_NAME),
                            memoryManagerService.newSchedulerDate(sharedDataEntity.getTokenExpireTime()));

            if (StringUtils.hasText(code)) {
                memoryManagerService.editFrom(MemoryType.AVAILABLE_CODES, code,
                        memoryManagerService.newSchedulerDate(sharedDataEntity.getOneTimeCodeMinutes()));
            }

            return recoverStoredRequest(request, response);
        }
        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }


    public ResponseEntity<?> authByOneTimeCode(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (memoryManagerService.viewFrom(MemoryType.AVAILABLE_CODES).containsKey(code)) {

            //Extract newSchedulerdate
            memoryManagerService.editFrom(AVAILABLE_TOKENS,
                    injectCookie(request, response, PROXY_TOKEN_NAME),
                    memoryManagerService.newSchedulerDate(sharedDataEntity.getTokenExpireTime()));

            memoryManagerService.removeFrom(MemoryType.AVAILABLE_CODES, code);
            return recoverStoredRequest(request, response);
        }

        throw new ConnectionException("You shouldn´t be here. To the lobby!");
    }

    public void resetClientByClearingCookies(HttpServletRequest request, HttpServletResponse response) {
        clearCookie(request, PROXY_UUID_NAME.name(), response);
        clearCookie(request, PROXY_TOKEN_NAME.name(), response);
    }

    private ResponseEntity<?> recoverStoredRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //If it blows, it will redirect to login
        Cookie cookieUUID = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equalsIgnoreCase(PROXY_UUID_NAME.name())).findFirst().get();

        Properties properties = (Properties) memoryManagerService.viewFrom(WAITING_USERS).get(cookieUUID.getValue());

        clearCookie(request, PROXY_UUID_NAME.name(), response);

        return handleRequest((String) properties.get("body"),
                (HttpMethod) properties.get("method"), (HttpServletRequest) properties.get("request"),
                response);
    }

    private String injectCookie(HttpServletRequest request, HttpServletResponse response, CookieType cookieType) {
        String random = memoryManagerService.createRandomFor(request.getRemoteAddr());
        response.addCookie(new Cookie(cookieType.name(), random));
        return random;
    }

    private void clearCookie(HttpServletRequest request, String randomName, HttpServletResponse response) {
        AuthUtils.clearCookie(request, randomName, response,
                cookieValue -> memoryManagerService.removeFrom(
                        CookieType.valueOf(randomName).getValue(), cookieValue));
    }


}
