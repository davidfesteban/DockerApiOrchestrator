package de.naivetardis.landscaper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Container;
import de.naivetardis.landscaper.dto.dockerjava.DockerDefinitionEntity;
import de.naivetardis.landscaper.exception.ConnectionException;
import de.naivetardis.landscaper.outcomponent.interfaces.DockerStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class DockerOrchestrator {

    private DockerStatusRepository dockerStatusRepository;
    private ObjectMapper objectMapper;

    public void saveCurrentRunningDockerStatus(List<Container> containers) {
        dockerStatusRepository.saveRunningInstances(containers);
    }

    public void loadDockerDefinition(File changedFile) {
        try {
            dockerStatusRepository.saveDockerDefinitionEntities(objectMapper.readValue(changedFile, DockerDefinitionEntity.class));
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    public void loadDockerDefinition(List<File> files) {
        files.forEach(this::loadDockerDefinition);
    }

    public void loadDockerDefinition(ChangedFile ale) {
        loadDockerDefinition(ale.getFile());
        //TODO: changedFile Type.Delete Type.Add Type.Update...
    }


}
