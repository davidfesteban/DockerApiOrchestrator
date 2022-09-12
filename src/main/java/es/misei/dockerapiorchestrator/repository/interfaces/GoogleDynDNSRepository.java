package es.misei.dockerapiorchestrator.repository.interfaces;

import org.springframework.stereotype.Repository;

@Repository
public interface GoogleDynDNSRepository {

    Boolean updateIpAddress(String ip);
}
