package gr.ianic.model.rules;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.Set;

@Entity(defaultKeyspace = "rules_keyspace")
@CqlName("rules")
public class Rule {
    @PartitionKey()
    @CqlName("tenant")
    String tenant;
    @PartitionKey(1)
    @CqlName("mode")
    String mode;
    @ClusteringColumn
    @CqlName("name")
    String name;
    @CqlName("description")
    String description;
    @CqlName("entryPoints")
    Set<String> entryPoints;
    @CqlName("entities")
    Set<String> entities;
    @CqlName("drl")
    String drl;

    public Rule() {
    }

    public Rule(String tenant, String mode, String name, String description, Set<String> entryPoints, Set<String> entities, String drl) {
        this.tenant = tenant;
        this.mode = mode;
        this.name = name;
        this.description = description;
        this.entryPoints = entryPoints;
        this.entities = entities;
        this.drl = drl;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    public Set<String> getEntryPoints() {
        return entryPoints;
    }

    public void setEntryPoints(Set<String> entryPoints) {
        this.entryPoints = entryPoints;
    }

    public Set<String> getEntities() {
        return entities;
    }

    public void setEntities(Set<String> entities) {
        this.entities = entities;
    }

    public String getDrl() {
        return drl;
    }

    public void setDrl(String drl) {
        this.drl = drl;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "tenant:'" + tenant + '\'' +
                ", mode:'" + mode + '\'' +
                ", name:'" + name + '\'' +
                ", description:'" + description + '\'' +
                ", entryPoints:" + entryPoints +
                ", entities:" + entities +
                ", drl:'" + drl + '\'' +
                '}';
    }
}
