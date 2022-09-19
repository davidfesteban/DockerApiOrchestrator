package de.naivetardis.landscaper.jobs;

import com.github.dockerjava.api.DockerClient;
import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.service.DockerOrchestrator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class DockerListener {

    private ApplicationContext applicationContext;

    private DockerOrchestrator dockerOrchestrator;
    private DockerClient dockerClient;

    @Scheduled(initialDelay = 5, fixedRateString = "${docker-listener.seconds}", timeUnit = TimeUnit.SECONDS)
    public void saveCurrentRunningDockerStatus() {
        dockerOrchestrator.saveCurrentRunningDockerStatus(dockerClient.listContainersCmd()
                .withShowSize(true)
                .withShowAll(true)
                .withStatusFilter(List.of("created","restarting","running")).exec());
    }

    @PostConstruct
    public void startUpLoadDefinitions() throws IOException {
        dockerOrchestrator.loadDockerDefinition(
                Arrays.stream(applicationContext.getResources("classpath:/*/*.json"))
                        .map((Function<Resource, File>) resource -> {
                            try {
                                return resource.getFile();
                            } catch (IOException e) {
                                throw new ConnectionException(e);
                            }
                        }).collect(Collectors.toList()));
    }

}
