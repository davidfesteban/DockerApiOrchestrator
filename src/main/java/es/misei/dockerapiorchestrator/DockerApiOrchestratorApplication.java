package es.misei.dockerapiorchestrator;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@AllArgsConstructor
@EnableScheduling
@EnableRetry
public class DockerApiOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerApiOrchestratorApplication.class, args);
    }

}
