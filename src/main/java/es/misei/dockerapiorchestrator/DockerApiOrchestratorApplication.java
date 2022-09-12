package es.misei.dockerapiorchestrator;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class DockerApiOrchestratorApplication {


	public static void main(String[] args) {
		SpringApplication.run(DockerApiOrchestratorApplication.class, args);
	}

}
