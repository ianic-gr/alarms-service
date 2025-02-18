package gr.ianic.model.rules;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Entity(defaultKeyspace = "rules_keyspace")
@CqlName("rules")
public class Rule {
    @PartitionKey()
    @CqlName("tenant")
    String tenant;
    @ClusteringColumn
    @CqlName("name")
    String name;
    @CqlName("description")
    String description;
    @PartitionKey(1)
    @CqlName("type")
    String type;
    @CqlName("rule")
    String rule;
    @PartitionKey(2)
    @CqlName("source")
    String source;

    public Rule() {
    }

    public Rule(String tenant, String source, String name, String description, String type, String rule) {
        this.tenant = tenant;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rule = rule;
        this.source = source;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "tenant='" + tenant + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", rule='" + rule + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
