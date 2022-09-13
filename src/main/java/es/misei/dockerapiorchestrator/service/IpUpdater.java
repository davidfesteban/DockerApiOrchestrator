package es.misei.dockerapiorchestrator.service;

import es.misei.dockerapiorchestrator.repository.interfaces.GoogleDynDNSRepository;
import org.springframework.stereotype.Service;

@Service
public class IpUpdater {

    private final GoogleDynDNSRepository googleDynDNSRepository;
    private String previousIp;

    public IpUpdater(GoogleDynDNSRepository googleDynDNSRepository, String previousIp) {
        this.googleDynDNSRepository = googleDynDNSRepository;
        this.previousIp = previousIp;
    }

    public void updateIpOnGoogleDynDns(String ip) {
        if(previousIp.equalsIgnoreCase(ip)) {
            previousIp = ip;
            googleDynDNSRepository.updateIpAddress(ip);
        }
    }
}
