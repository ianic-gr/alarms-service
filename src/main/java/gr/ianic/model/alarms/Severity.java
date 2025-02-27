package gr.ianic.model.alarms;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the severity levels of an alarm.
 * This enum defines two severity levels: WARNING and CRITICAL.
 * It also provides utility methods to convert between string labels and enum values.
 */
public enum Severity {
    WARNING("WARNING"), // Represents a warning-level severity
    CRITICAL("CRITICAL"); // Represents a critical-level severity

    // A map to store enum values keyed by their string labels
    private static final Map<String, Severity> BY_LABEL = new HashMap<>();

    // Static block to populate the map with enum values
    static {
        for (Severity e : values()) {
            BY_LABEL.put(e.severity, e);
        }
    }

    private final String severity; // The string label for the severity level

    /**
     * Constructs a Severity enum with the given string label.
     *
     * @param severity The string label for the severity level.
     */
    Severity(String severity) {
        this.severity = severity;
    }

    /**
     * Returns the Severity enum corresponding to the given string label.
     *
     * @param label The string label to look up.
     * @return The corresponding Severity enum, or {@code null} if no match is found.
     */
    public static Severity valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

    /**
     * Returns the string label for this severity level.
     *
     * @return The string label for this severity level.
     */
    @Override
    public String toString() {
        return severity;
    }
}