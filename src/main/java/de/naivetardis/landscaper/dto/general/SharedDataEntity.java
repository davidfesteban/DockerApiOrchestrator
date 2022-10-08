package de.naivetardis.landscaper.dto.general;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class SharedDataEntity {

    @Value("${master.user}")
    private String user;
    @Value("${master.pass}")
    private String pass;
    @Value("${scheduler.waiting-user.minutes}")
    private Integer waitingUserMinutes;
    @Value("${scheduler.auth-tries.minutes}")
    private Integer authTriesMinutes;
    @Value("${scheduler.auth-tries.number}")
    private Integer authNumberTries;
    @Value("${scheduler.one-time-code.minutes}")
    private Integer oneTimeCodeMinutes;
    @Value("${scheduler.token.minutes}")
    private Integer tokenExpireTime;
    @Value("${host.url}")
    private String hostUrl;

    public String getHostUrl() {
        return hostUrl;
    }

    public Integer getAuthNumberTries() {
        return authNumberTries;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public Integer getWaitingUserMinutes() {
        return waitingUserMinutes;
    }

    public Integer getAuthTriesMinutes() {
        return authTriesMinutes;
    }

    public Integer getOneTimeCodeMinutes() {
        return oneTimeCodeMinutes;
    }

    public Integer getTokenExpireTime() {
        return tokenExpireTime;
    }
}
