package de.naivetardis.landscaper.outcomponent.interfaces;

import com.github.dockerjava.api.model.Container;
import de.naivetardis.landscaper.dto.dockerjava.DockerDefinitionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DockerStatusRepository {

    void saveRunningInstances(List<Container> containers);
    void saveDockerDefinitionEntities(List<DockerDefinitionEntity> entities);

    void saveDockerDefinitionEntities(DockerDefinitionEntity dockerDefinitionEntity);

}
