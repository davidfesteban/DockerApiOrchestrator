package de.naivetardis.landscaper.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
public class GoogleDynDNSEntity {

    @Value("${google-dyn-dns.user}")
    private String user;
    @Value("${google-dyn-dns.pass}")
    private String pass;
    @Value("${google-dyn-dns.hostname}")
    private String hostname;

    public String getAuth() {
        return String.format("Basic %s:%s", user, pass);
    }
}
