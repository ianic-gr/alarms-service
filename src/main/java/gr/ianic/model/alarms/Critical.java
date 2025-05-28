package gr.ianic.model.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

/**
 * Represents a critical alarm with properties such as severity, message, timestamp, and count.
 * This class implements the {@link Alarm} interface and provides specific behavior for critical alarms.
 */
public class Critical implements Alarm {

    private Severity severity; // The severity level of the alarm
    private String message; // The message associated with the alarm
    private Long datetime; // The timestamp of the alarm
    private Integer count; // The count of occurrences for the alarm
    private String key; // The key associated with the alarm

    /**
     * Default constructor initializes the count to 0 and sets the timestamp to the current time.
     */
    public Critical() {
        this.count = 0;
        this.datetime = new Date().getTime();
    }

    /**
     * Constructs a critical alarm with the specified severity and message.
     * The timestamp is set to the current time, and the count is initialized to 0.
     *
     * @param message  The message associated with the alarm.
     */
    public Critical(String message) {
        this.severity = Severity.CRITICAL;
        this.message = message;
        this.datetime = new Date().getTime();
        this.count = 0;
    }

    /**
     * Constructs a critical alarm with the specified severity, message, and timestamp.
     * The count is initialized to 0.
     *
     * @param message  The message associated with the alarm.
     * @param datetime The timestamp of the alarm.
     */
    public Critical(String message, Long datetime) {
        this.severity = Severity.CRITICAL;
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
        return "critical-alarms"; // The Kafka topic for critical alarms
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
     * Returns a JSON representation of the critical alarm.
     *
     * @return A JSON string representing the critical alarm.
     * @throws RuntimeException If JSON serialization fails.
     */
    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Critical to JSON", e);
        }
    }
}
