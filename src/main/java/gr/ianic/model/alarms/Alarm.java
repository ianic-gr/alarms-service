package gr.ianic.model.alarms;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents an alarm with properties such as severity, message, timestamp, and count.
 * This interface defines the common behavior and properties for all alarm types.
 */
public interface Alarm {

    // Jackson ObjectMapper configured to ignore unknown properties during deserialization
    ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Sets the severity of the alarm.
     *
     * @param severity The severity level of the alarm.
     */
    void setSeverity(Severity severity);

    /**
     * Gets the severity of the alarm.
     *
     * @return The severity level of the alarm as a string.
     */
    String getSeverity();

    /**
     * Gets the message associated with the alarm.
     *
     * @return The alarm message.
     */
    String getMessage();

    /**
     * Sets the message associated with the alarm.
     *
     * @param message The alarm message.
     */
    void setMessage(String message);

    /**
     * Gets the timestamp of the alarm.
     *
     * @return The timestamp of the alarm as a long value (e.g., epoch time).
     */
    Long getDateTime();

    /**
     * Sets the timestamp of the alarm.
     *
     * @param datetime The timestamp of the alarm as a long value (e.g., epoch time).
     */
    void setDateTime(Long datetime);

    /**
     * Gets the count of occurrences for the alarm.
     *
     * @return The count of occurrences as an integer.
     */
    Integer getCount();

    /**
     * Sets the count of occurrences for the alarm.
     *
     * @param count The count of occurrences as an integer.
     */
    void setCount(Integer count);

    /**
     * Gets the topic associated with the alarm.
     *
     * @return The topic name as a string.
     */
    String getTopic();

    /**
     * Gets the key associated with the alarm.
     *
     * @return The key as a string.
     */
    String getKey();

    /**
     * Sets the key associated with the alarm.
     *
     * @param key The key as a string.
     */
    void setKey(String key);

    /**
     * Returns a string representation of the alarm.
     *
     * @return A string representation of the alarm.
     */
    String toString();
}