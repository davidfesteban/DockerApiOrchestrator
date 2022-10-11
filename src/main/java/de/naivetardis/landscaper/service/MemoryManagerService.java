package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.dto.net.HttpRequestEntity;
import de.naivetardis.landscaper.utility.AuthUtils;
import de.naivetardis.landscaper.utility.RandomString;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class MemoryManagerService {
    private final RandomString randomString = new RandomString(30);
    private final Map<String, String> uuids = new HashMap<>();

    private final Map<String, String> tokens = new HashMap<>();
    private final Map<String, HttpRequestEntity> storedRequests = new HashMap<>();

    private final Set<String> codes = new HashSet<>();

    public String generateUUIDFrom(String remoteAddr) {
        String random = randomString.nextString();

        if (uuids.containsKey(remoteAddr)) {
            random = uuids.get(remoteAddr);
        } else {
            uuids.put(remoteAddr, random);
        }

        return random;
    }

    public String generateTokenFrom(String remoteAddr) {
        String random = randomString.nextString();

        if (tokens.containsKey(remoteAddr)) {
            random = tokens.get(remoteAddr);
        } else {
            tokens.put(remoteAddr, random);
        }

        return random;
    }

    public void storeRequestFromUser(String uuid, HttpRequestEntity buildFrom) {
        storedRequests.put(uuid, buildFrom);
    }

    public void holdAndWaitForAuth(HttpServletRequest request) throws InterruptedException {
        //IT MUST BE ON MEMORY
        String UUID = AuthUtils.getCookie(request, AuthUtils.CookieType.PROXY_UUID_NAME.name()).get().getValue();

        synchronized (this) {
            while (storedRequests.containsKey(UUID)) {
                this.wait();
            }
        }
    }

    public boolean userWaitingForAuth(String cookieValue) {
        return storedRequests.containsKey(cookieValue);
    }

    public void storeSingleCodeSession(String code) {
        codes.add(code);
    }

    public boolean isCodeValid(String code) {
        return codes.contains(code);
    }

    public void unblockPreviousRequestsWithUUID(String uuid) {
        log.info("Releasing lock for uuid: {}", uuid);
        synchronized (this) {
            storedRequests.remove(uuid);
            this.notifyAll();
        }
    }

    public HttpRequestEntity recoverStoredRequest(String uuid) {
        return storedRequests.get(uuid);
    }

    public boolean isTokenValid(String token) {
        //TODO:CHECK IP
        return tokens.containsValue(token);
    }

    public boolean isAddressOnToken(String address) {
        return tokens.containsKey(address);
    }

    public void removeCodeSession(String code) {
        codes.remove(code);
    }
}
