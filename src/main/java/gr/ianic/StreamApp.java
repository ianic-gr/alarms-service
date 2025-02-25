package gr.ianic;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Startup
@ApplicationScoped
public class StreamApp {

    @Inject
    SessionManager sessionManager;

    @Inject
    RulesDao rulesDao;

    List<Rule> rules;


    @PostConstruct
    public void init() {
        rules = rulesDao.getByMode("stream").all();

        Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizedRules = organizeRules(rules);

        organizedRules.forEach((tenant, erTuple) -> {
            System.out.println("Tenant: " + tenant);
            sessionManager.createStreamSession(erTuple.getKey(), tenant, erTuple.getValue());
            erTuple.getValue().forEach(rule -> System.out.println("    Rule: " + rule.getName()));
        });
    }

    public Map<String, Map<String, List<Rule>>> organizeRulesByTenantAndEntrypoint(@NotNull List<Rule> rules) {
        // Map to store the result
        Map<String, Map<String, List<Rule>>> tenantMap = new HashMap<>();

        for (Rule rule : rules) {
            String tenant = rule.getTenant();
            Set<String> entrypoints = rule.getEntryPoints();

            // If the tenant is not already in the map, add them
            tenantMap.putIfAbsent(tenant, new HashMap<>());

            // Get the map of entry points to rules for the current tenant
            Map<String, List<Rule>> entrypointMap = tenantMap.get(tenant);

            // Iterate through each entrypoint and add the rule to the corresponding list
            for (String entrypoint : entrypoints) {
                entrypointMap.putIfAbsent(entrypoint, new ArrayList<>());
                entrypointMap.get(entrypoint).add(rule);
            }
        }

        return tenantMap;
    }

    public Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> flattenTenantRules(@NotNull Map<String, Map<String, List<Rule>>> tenantMap) {
        Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> result = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Rule>>> tenantEntry : tenantMap.entrySet()) {
            String tenant = tenantEntry.getKey();
            Map<String, List<Rule>> entrypointMap = tenantEntry.getValue();

            // Collect all unique entry points
            Set<String> allEntrypoints = new HashSet<>(entrypointMap.keySet());

            // Collect all rules (flattened)
            List<Rule> allRules = new ArrayList<>();
            for (List<Rule> rules : entrypointMap.values()) {
                allRules.addAll(rules);
            }

            // Add to the result map
            result.put(tenant, new AbstractMap.SimpleEntry<>(allEntrypoints, allRules));
        }

        return result;
    }

    public Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizeRules(List<Rule> rules) {
        Map<String, Map<String, List<Rule>>> tenantMap = organizeRulesByTenantAndEntrypoint(rules);
        return flattenTenantRules(tenantMap);
    }

}
