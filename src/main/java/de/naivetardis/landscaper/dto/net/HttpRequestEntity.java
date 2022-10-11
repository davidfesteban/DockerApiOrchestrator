package de.naivetardis.landscaper.dto.net;

import de.naivetardis.landscaper.utility.AuthUtils;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Getter
public class HttpRequestEntity {
    private final Map<String, String> headers;
    private String body;
    private String requestUrl;
    private String requestUri;
    private List<Cookie> cookies;
    private String ip;
    private HttpMethod method;
    private ScheduledFuture<?> date;
    private String serverName;
    private String queryString;

    private HttpRequestEntity() {
        body = "";
        headers = new HashMap<String, String>();
        cookies = new ArrayList<>();
    }

    public static HttpRequestEntity buildFrom(String body, HttpMethod method, HttpServletRequest request) {
        return buildFrom(body, method, request, null);
    }

    public static HttpRequestEntity buildFrom(String body, HttpMethod method, HttpServletRequest request, ScheduledFuture<?> newSchedulerDate) {
        HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
        httpRequestEntity.body = body;
        httpRequestEntity.requestUrl = request.getRequestURL().toString();
        httpRequestEntity.requestUri = request.getRequestURI();
        request.getHeaderNames().asIterator().forEachRemaining(s -> httpRequestEntity.headers.put(s, request.getHeader(s)));

        if (AuthUtils.hasCookies(request)) {
            httpRequestEntity.cookies = List.of(request.getCookies());
        }

        httpRequestEntity.ip = request.getRemoteAddr();
        httpRequestEntity.method = method;
        httpRequestEntity.date = newSchedulerDate;
        httpRequestEntity.serverName = request.getServerName();
        httpRequestEntity.queryString = request.getQueryString();
        return httpRequestEntity;
    }
}
