package de.naivetardis.landscaper.utility;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class AuthUtils {

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        if (hasCookies(request)) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(cookieName))
                    .findFirst();
        }

        return Optional.of(null);

    }

    public static boolean hasCookie(HttpServletRequest request, String cookieName) {
        if (hasCookies(request)) {
            return getCookie(request, cookieName).isPresent();
        }

        return false;
    }

    public static boolean hasParameter(HttpServletRequest request, String cookieName) {
        return StringUtils.hasText((String) request.getSession().getAttribute(cookieName));
    }

    public static String getParameter(HttpServletRequest request, String cookieName) {
        return (String) request.getSession().getAttribute(cookieName);
    }

    public static String clearCookie(HttpServletRequest request,
                                     String cookieName,
                                     HttpServletResponse response) {
        Optional<Cookie> cookieObject = getCookie(request, cookieName);

        if (cookieObject.isPresent()) {
            Cookie cookie = cookieObject.get();
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return cookie.getValue();
        }

        return null;
    }

    public static boolean isValidAuth(String sentEmail, String sentPass, String validEmail, String validPass) {
        return StringUtils.hasText(sentEmail) && StringUtils.hasText(sentPass) &&
                sentEmail.equalsIgnoreCase(validEmail) && sentPass.equalsIgnoreCase(validPass);
    }

    public static boolean hasCookies(HttpServletRequest request) {
        return request.getCookies() != null && request.getCookies().length > 0;
    }

    public static ResponseEntity<String> loginView() throws IOException {
        return new ResponseEntity<String>(Files.readString(Path.of("src/main/resources/web/login.html"), Charset.defaultCharset()), HttpStatus.OK);
    }

    public static boolean isPublicSubdomain(HttpServletRequest request) {
        return request.getServerName().startsWith("public");
    }

    public static void removeAllParameters(HttpServletRequest request) {
        request.removeAttribute(CookieType.PROXY_UUID_NAME.name());
        request.removeAttribute(CookieType.PROXY_TOKEN_NAME.name());
    }

    public enum CookieType {
        PROXY_TOKEN_NAME,
        PROXY_UUID_NAME;
    }
}
