package gr.ianic.model.measurements;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScadaMeasurement {
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty
    private String time;
    @JsonProperty
    private Double value;
    @JsonProperty
    private Double consumption = null;
    @JsonProperty
    private String sensorName;
    @JsonProperty
    private String variableName;
    @JsonProperty
    private String variableType;
    @JsonProperty
    private String sensorType;
    @JsonProperty
    private String qualityCode;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getQualityCode() {
        return qualityCode;
    }

    public void setQualityCode(String qualityCode) {
        this.qualityCode = qualityCode;
    }

    @Override
    @SneakyThrows
    public String toString() {
        return mapper.writeValueAsString(this);
    }
}
