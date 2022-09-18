package de.naivetardis.landscaper.service;

import de.naivetardis.landscaper.repository.interfaces.GoogleDynDNSRepository;
import org.springframework.stereotype.Service;

@Service
public class IpUpdater {

    private final GoogleDynDNSRepository googleDynDNSRepository;
    private String previousIp;

    public IpUpdater(GoogleDynDNSRepository googleDynDNSRepository) {
        this.googleDynDNSRepository = googleDynDNSRepository;
        this.previousIp = "";
    }

    public void updateIpOnGoogleDynDns(String ip) {
        if(!previousIp.equalsIgnoreCase(ip)) {
            previousIp = ip;
            googleDynDNSRepository.updateIpAddress(ip);
        }
    }
}
