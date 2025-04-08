package gr.ianic.rules;

import gr.ianic.model.rules.Rule;

import java.util.List;
import java.util.Set;

// Flattened result holder
public class TenantRulesInfo {
    private Set<String> entrypoints;
    private Set<String> entities;
    private List<Rule> rules;

    public TenantRulesInfo(Set<String> entrypoints, Set<String> entities, List<Rule> rules) {
        this.entrypoints = entrypoints;
        this.entities = entities;
        this.rules = rules;
    }

    public Set<String> getEntrypoints() {
        return entrypoints;
    }

    public Set<String> getEntities() {
        return entities;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
