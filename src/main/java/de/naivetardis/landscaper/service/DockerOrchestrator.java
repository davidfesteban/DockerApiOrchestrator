package de.naivetardis.landscaper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class DockerOrchestrator {

    private final Set<Container> containerList;
    private ObjectMapper objectMapper;

    public Pair<Integer, Integer> retrievePortFromImageName(String name) {
        return containerList.stream()
                .filter(container -> Arrays.stream(container.getNames()).anyMatch(s -> s.equalsIgnoreCase(name)))
                .findFirst()
                .map(container -> container.getPorts()[0])
                .map((Function<ContainerPort, Pair<Integer, Integer>>) container -> new ImmutablePair<>(container.getPublicPort(), container.getPrivatePort()))
                .get();
    }

    public void saveCurrentRunningDockerStatus(List<Container> containers) {

    }

    public void loadDockerDefinition(File changedFile) {
        //dockerStatusRepository.saveDockerDefinitionEntities(objectMapper.readValue(changedFile, DockerDefinitionEntity.class));
    }

    public void loadDockerDefinition(List<File> files) {
        //files.forEach(this::loadDockerDefinition);
    }

    public void loadDockerDefinition(ChangedFile ale) {
        //loadDockerDefinition(ale.getFile());
        //TODO: changedFile Type.Delete Type.Add Type.Update...
    }


}
