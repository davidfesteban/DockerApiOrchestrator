package de.naivetardis.landscaper.mapper;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerHostConfig;
import de.naivetardis.landscaper.dto.docker.DockerDefinitionEntity;
import de.naivetardis.landscaper.dto.docker.Port;
import lombok.experimental.UtilityClass;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.*;

@UtilityClass
public class DockerDefinitionMapper {

    public DockerDefinitionEntity from(Container container) {
        final DockerDefinitionEntity dockerDefinitionEntity = new DockerDefinitionEntity();
        dockerDefinitionEntity.setName(stream(container.getNames()).findFirst().orElse("DefaultName"));
        dockerDefinitionEntity.setImage(container.getImage());
        dockerDefinitionEntity.setNetwork(requireNonNullElse(container.getHostConfig(), new ContainerHostConfig().withNetworkMode("")).getNetworkMode());
        dockerDefinitionEntity.setPorts(stream(container.getPorts()).map(containerPort -> {
            final Port port = new Port();
            port.setHost(valueOf(containerPort.getPublicPort()));
            port.setContainer(valueOf(containerPort.getPrivatePort()));
            return port;
        }).collect(toList()));
        //TODO: Fill
        return dockerDefinitionEntity;
    }
}
