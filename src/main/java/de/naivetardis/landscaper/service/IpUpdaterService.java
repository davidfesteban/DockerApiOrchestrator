package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.bridge.GoogleDynDNSBridge;
import org.springframework.stereotype.Service;

@Service
public class IpUpdaterService {

    private final GoogleDynDNSBridge googleDynDNSBridgeRepository;
    private String previousIp;

    public IpUpdaterService(GoogleDynDNSBridge googleDynDNSBridgeRepository) {
        this.googleDynDNSBridgeRepository = googleDynDNSBridgeRepository;
        this.previousIp = "";
    }

    public void updateIpOnGoogleDynDns(String ip) {
        if (!previousIp.equalsIgnoreCase(ip)) {
            previousIp = ip;
            googleDynDNSBridgeRepository.updateIpAddress(ip);
        }
    }
}
