package gr.ianic;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

/**
 * The main application class for managing stream sessions and rules.
 * This class initializes the application by loading rules and creating stream sessions for each tenant.
 */
@Startup
@ApplicationScoped
public class StreamApp {

    @Inject
    SessionManager sessionManager; // Manages stream sessions

    @Inject
    RulesDao rulesDao; // Data Access Object for rules

    /**
     * Initializes the application by loading rules and creating stream sessions.
     * This method is automatically called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        // Fetch all rules with mode "stream"
        // List of rules to be applied
        List<Rule> rules = rulesDao.getByMode("stream").all();

        // Organize rules by tenant and entry points
        Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizedRules = sessionManager.organizeRules(rules);

        // Create a stream session for each tenant
        organizedRules.forEach((tenant, erTuple) -> {
            boolean created = sessionManager.createStreamSession(erTuple.getKey(), tenant, erTuple.getValue());
            if (!created) {
                System.out.println("Create stream session failed for tenant: " + tenant);
            }
            // Print the rules for the tenant
            erTuple.getValue().forEach(rule -> System.out.println("    Rule: " + rule.getName()));
        });
    }
}