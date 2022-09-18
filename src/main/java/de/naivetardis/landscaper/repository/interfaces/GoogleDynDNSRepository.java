package de.naivetardis.landscaper.repository.interfaces;

import de.naivetardis.landscaper.annotation.Retryable;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleDynDNSRepository {
    void updateIpAddress(String ip);
}
