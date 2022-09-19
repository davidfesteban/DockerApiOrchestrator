package de.naivetardis.landscaper.outcomponent.interfaces;

import org.springframework.stereotype.Repository;

@Repository
public interface GoogleDynDNSRepository {
    void updateIpAddress(String ip);
}
