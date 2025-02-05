package gr.ianic.model.alarms;

import java.util.HashMap;
import java.util.Map;

public enum Severity {
    WARNING("WARNING"),
    CRITICAL("CRITICAL");

    private static final Map<String, Severity> BY_LABEL = new HashMap<>();

    static {
        for (Severity e : values()) {
            BY_LABEL.put(e.severity, e);
        }
    }

    private final String severity;

    Severity(String severity) {
        this.severity = severity;
    }

    public static Severity valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

    @Override
    public String toString() {
        return severity;
    }
}
