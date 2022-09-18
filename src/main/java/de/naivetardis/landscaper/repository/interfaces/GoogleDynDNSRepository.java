package de.naivetardis.landscaper.repository.interfaces;

import org.springframework.stereotype.Repository;

@Repository
public interface GoogleDynDNSRepository {
    void updateIpAddress(String ip);
}
