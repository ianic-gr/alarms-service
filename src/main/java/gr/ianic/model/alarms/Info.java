package gr.ianic.model.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

/**
 * Represents an informational alarm with properties such as severity, message, timestamp, and count.
 * This class implements the {@link Alarm} interface and provides specific behavior for info-level alarms.
 */
public class Info implements Alarm {

    private Severity severity; // The severity level of the info message
    private String message;    // The message associated with the info
    private Long datetime;     // The timestamp of the info
    private Integer count;     // The count of occurrences
    private String key;        // The key associated with the info

    /**
     * Default constructor initializes the count to 0 and sets the timestamp to the current time.
     */
    public Info() {
        this.count = 0;
        this.datetime = new Date().getTime();
    }

    /**
     * Constructs an informational alarm with the specified severity and message.
     *
     * @param message  The message associated with the info.
     */
    public Info(String message) {
        this.severity = Severity.INFO;
        this.message = message;
        this.datetime = new Date().getTime();
        this.count = 0;
    }

    /**
     * Constructs an informational alarm with the specified severity, message, and timestamp.
     *
     * @param message  The message associated with the info.
     * @param datetime The timestamp of the info.
     */
    public Info(String message, Long datetime) {
        this.severity = Severity.INFO;
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
        return "info-alarms"; // The Kafka topic for info alarms
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns a JSON representation of the info.
     *
     * @return A JSON string representing the info.
     * @throws RuntimeException If JSON serialization fails.
     */
    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Info to JSON", e);
        }
    }
}
