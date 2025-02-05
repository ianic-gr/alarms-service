package gr.ianic.model.rules;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity(defaultKeyspace = "rules_keyspace")
@CqlName("rules")
public class Rule {
    @CqlName("tenant")
    String tenant;
    @CqlName("name")
    String name;
    @CqlName("description")
    String description;
    @CqlName("type")
    String type;
    @CqlName("rule")
    String rule;

    public Rule() {
    }

    public Rule(String tenant, String name, String description, String type, String rule) {
        this.tenant = tenant;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rule = rule;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
