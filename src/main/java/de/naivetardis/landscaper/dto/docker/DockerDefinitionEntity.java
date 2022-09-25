
package de.naivetardis.landscaper.dto.docker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "image",
    "name",
    "network",
    "ports",
    "volumes",
    "restart",
    "others",
    "environment"
})
@Generated("jsonschema2pojo")
public class DockerDefinitionEntity {

    @JsonProperty("image")
    private String image;
    @JsonProperty("name")
    private String name;
    @JsonProperty("network")
    private String network;
    @JsonProperty("ports")
    private List<Port> ports = null;
    @JsonProperty("volumes")
    private List<Volume> volumes = null;
    @JsonProperty("restart")
    private String restart;
    @JsonProperty("others")
    private List<Other> others = null;
    @JsonProperty("environment")
    private List<Environment> environment = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("image")
    public String getImage() {
        return image;
    }

    @JsonProperty("image")
    public void setImage(String image) {
        this.image = image;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("network")
    public String getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(String network) {
        this.network = network;
    }

    @JsonProperty("ports")
    public List<Port> getPorts() {
        return ports;
    }

    @JsonProperty("ports")
    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    @JsonProperty("volumes")
    public List<Volume> getVolumes() {
        return volumes;
    }

    @JsonProperty("volumes")
    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    @JsonProperty("restart")
    public String getRestart() {
        return restart;
    }

    @JsonProperty("restart")
    public void setRestart(String restart) {
        this.restart = restart;
    }

    @JsonProperty("others")
    public List<Other> getOthers() {
        return others;
    }

    @JsonProperty("others")
    public void setOthers(List<Other> others) {
        this.others = others;
    }

    @JsonProperty("environment")
    public List<Environment> getEnvironment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(List<Environment> environment) {
        this.environment = environment;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DockerDefinitionEntity that = (DockerDefinitionEntity) o;
        return image.equalsIgnoreCase(that.image) && name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, name);
    }
}
