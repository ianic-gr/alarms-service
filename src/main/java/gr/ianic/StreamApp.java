package gr.ianic;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import gr.ianic.rules.TenantRulesInfo;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

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
        @NotNull Map<String, TenantRulesInfo> organizedRules = sessionManager.organizeRules(rules);

        // Create a stream session for each tenant
        organizedRules.forEach((tenant, info) -> {
            boolean created = sessionManager.createStreamSession(tenant, info);
            if (!created) {
                System.out.println("Create stream session failed for tenant: " + tenant);
            }
            // Print the rules for the tenant
            info.getRules().forEach(rule -> System.out.println("    Rule: " + rule.getName()));
        });
    }
}