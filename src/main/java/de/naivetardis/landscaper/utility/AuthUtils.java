package de.naivetardis.landscaper.utility;

import de.naivetardis.landscaper.service.CachedMemoryService.MemoryType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

public class AuthUtils {

    public static void clearCookie(HttpServletRequest request,
                                   String cookieName,
                                   HttpServletResponse response,
                                   Consumer<String> memoryAction) {
        if (hasCookies(request)) {
            Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase(cookieName))
                    .findFirst().ifPresent((cookie -> {
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                        memoryAction.accept(cookie.getValue());
                    }));
        }
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



    public static Properties requestToProperties(String body, HttpMethod httpMethod, HttpServletRequest request, ScheduledFuture<?> date) {
        Properties properties = new Properties();

        //TODO: Names
        properties.put("body", Default.of(body).orElse(""));
        properties.put("method", Default.of(httpMethod).orElse(HttpMethod.GET));
        properties.put("request", request);
        properties.put("date", date);

        return properties;
    }

    public enum CookieType {
        PROXY_TOKEN_NAME(MemoryType.AVAILABLE_TOKENS),
        PROXY_UUID_NAME(MemoryType.WAITING_USERS);

        private final MemoryType value;

        // private enum constructor
        private CookieType(MemoryType value) {
            this.value = value;
        }

        public MemoryType getValue() {
            return value;
        }
    }
}
