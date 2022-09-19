package de.naivetardis.landscaper.outcomponent.impl;

import com.github.dockerjava.api.model.Container;
import de.naivetardis.landscaper.dto.dockerjava.DockerDefinitionEntity;
import de.naivetardis.landscaper.outcomponent.interfaces.DockerStatusRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DockerStatus implements DockerStatusRepository {

    private final Set<Container> containerList;
    private final Set<DockerDefinitionEntity> definitionEntityList;

    public DockerStatus () {
        containerList = new HashSet<>();
        definitionEntityList = new HashSet<>();
    }

    @Override
    public void saveRunningInstances(List<Container> containers) {
        containerList.addAll(containers);
    }

    @Override
    public void saveDockerDefinitionEntities(List<DockerDefinitionEntity> entities) {
        definitionEntityList.addAll(entities);
    }

    @Override
    public void saveDockerDefinitionEntities(DockerDefinitionEntity dockerDefinitionEntity) {
        definitionEntityList.add(dockerDefinitionEntity);
    }
}
