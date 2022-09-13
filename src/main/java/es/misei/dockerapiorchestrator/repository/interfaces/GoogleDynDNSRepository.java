package es.misei.dockerapiorchestrator.repository.interfaces;

import es.misei.dockerapiorchestrator.exception.ConnectionException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleDynDNSRepository {
    @Retryable(value = ConnectionException.class)
    void updateIpAddress(String ip);
}
