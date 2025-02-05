package gr.ianic.model.alarms;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Alarm {
    ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    void setSeverity(Severity severity);

    String getSeverity();

    String getMessage();

    void setMessage(String message);

    Long getDateTime();

    void setDateTime(Long datetime);

    Integer getCount();

    void setCount(Integer count);

    String toString();
}
