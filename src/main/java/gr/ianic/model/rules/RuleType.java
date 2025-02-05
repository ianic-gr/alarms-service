package gr.ianic.model.rules;

import java.util.HashMap;
import java.util.Map;

public enum RuleType {
    STREAM("STREAM"),
    SCHEDULE("SCHEDULE");

    private static final Map<String, RuleType> BY_LABEL = new HashMap<>();

    static {
        for (RuleType e : values()) {
            BY_LABEL.put(e.ruleType, e);
        }
    }

    private final String ruleType;

    RuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public static RuleType valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

    @Override
    public String toString() {
        return ruleType;
    }
}
