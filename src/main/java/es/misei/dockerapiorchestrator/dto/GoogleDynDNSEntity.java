package es.misei.dockerapiorchestrator.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@NoArgsConstructor
public class GoogleDynDNSEntity {

    @Value("${google-dyn-dns.user}")
    private String user;
    @Value("${google-dyn-dns.pass}")
    private String pass;
}
