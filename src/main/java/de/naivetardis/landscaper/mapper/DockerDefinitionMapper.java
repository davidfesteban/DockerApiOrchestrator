package de.naivetardis.landscaper.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerHostConfig;
import com.github.dockerjava.api.model.ContainerPort;
import de.naivetardis.landscaper.dto.dockerjava.DockerDefinitionEntity;
import de.naivetardis.landscaper.dto.dockerjava.Port;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
