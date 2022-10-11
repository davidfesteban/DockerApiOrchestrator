package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.general.SharedDataEntity;
import de.naivetardis.landscaper.dto.net.HttpRequestEntity;
import de.naivetardis.landscaper.utility.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.naivetardis.landscaper.utility.AuthUtils.*;

@Service
@AllArgsConstructor
@Slf4j
public class AuthManagerService {
    private MemoryManagerService memoryManagerService;
    private ProxyService proxyService;

    private SharedDataEntity dataEntity;

    public ResponseEntity<?> handleSingleRequest(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return proxyService.forwardWithProxyService(HttpRequestEntity.buildFrom(body, method, request), response);
    }

    public ResponseEntity<?> holdAndWaitForAuth(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        log.info("Holding request {}", request.getRequestURI());
        memoryManagerService.holdAndWaitForAuth(request);

        return proxyService.forwardWithProxyService(HttpRequestEntity.buildFrom(body, method, request), response);
    }

    public ResponseEntity<?> storeRequestAndShowLoginView(String body, HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String UUID = memoryManagerService.generateUUIDFrom(request.getRemoteAddr());
        response.addCookie(new Cookie(CookieType.PROXY_UUID_NAME.name(), UUID));
        memoryManagerService.storeRequestFromUser(UUID, HttpRequestEntity.buildFrom(body, method, request));

        log.info("Stored request {}", request.getRequestURI());
        return loginView();
    }

    public boolean userWaitingForAuth(HttpServletRequest request) {
        if (AuthUtils.hasCookie(request, CookieType.PROXY_UUID_NAME.name())) {
            return memoryManagerService.userWaitingForAuth(AuthUtils.getCookie(request, CookieType.PROXY_UUID_NAME.name()).get().getValue());
        }

        return false;
    }

    public boolean isAuthSuccessful(String email, String pass) {
        return isValidAuth(email, pass, dataEntity.getUser(), dataEntity.getPass());
    }

    public boolean isAuthSuccessful(String code) {
        return memoryManagerService.isCodeValid(code);
    }

    public void storeSingleCodeSession(String code) {
        memoryManagerService.storeSingleCodeSession(code);
    }

    public void injectUserToken(HttpServletRequest request, HttpServletResponse response) {
        String TOKEN = memoryManagerService.generateTokenFrom(request.getRemoteAddr());
        response.addCookie(new Cookie(CookieType.PROXY_TOKEN_NAME.name(), TOKEN));
    }

    public ResponseEntity<?> recoverStoredRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Recovering stored request");
        return proxyService.forwardWithProxyService(
                memoryManagerService.recoverStoredRequest(AuthUtils.getCookie(request, CookieType.PROXY_UUID_NAME.name()).get().getValue()),
                response);
    }

    public void removeSingleCodeSession(String code) {
        memoryManagerService.removeCodeSession(code);
    }

    public void unblockPreviousRequests(HttpServletRequest request, HttpServletResponse response) {
        String uuid = AuthUtils.getCookie(request, CookieType.PROXY_UUID_NAME.name()).get().getValue();
        Cookie cookie = new Cookie(CookieType.PROXY_UUID_NAME.name(), uuid);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        memoryManagerService.unblockPreviousRequestsWithUUID(uuid);
    }

    public boolean isAuthenticated(HttpServletRequest request) {

        if (AuthUtils.hasCookie(request, CookieType.PROXY_TOKEN_NAME.name())) {
            return memoryManagerService.isTokenValid(
                    AuthUtils.getCookie(request, CookieType.PROXY_TOKEN_NAME.name()).get().getValue());
        }

        return false;
    }
}
