package gr.ianic.model.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

public class Warning implements Alarm {

    private Severity severity;
    private String message;
    private Long datetime;
    private Integer count;
    private String key;

    public Warning() {
        count = 0;
        datetime = new Date().getTime();
    }

    public Warning(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
        this.datetime = new Date().getTime();
        this.count = 0;
    }

    public Warning(Severity severity, String message, Long datetime) {
        this.severity = severity;
        this.message = message;
        this.datetime = datetime;
        this.count = 0;
    }

    @Override
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String getSeverity() {
        return this.severity.toString();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Long getDateTime() {
        return this.datetime;
    }

    @Override
    public void setDateTime(Long datetime) {
        this.datetime = datetime;
    }

    @Override
    public Integer getCount() {
        return this.count;
    }

    @Override
    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String getTopic() {
        return "warning-alarms";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
