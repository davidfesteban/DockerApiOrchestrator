package de.naivetardis.landscaper.configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class DockerClientConfig {

    @Value("${docker.api.url}")
    private String dockerApi;


    @Bean
    public DockerClient dockerClient() {
        return DockerClientBuilder.getInstance(dockerApi).build();
    }

    @PreDestroy
    public void close() throws IOException {
        dockerClient().close();
    }
}
