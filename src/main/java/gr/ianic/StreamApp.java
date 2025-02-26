package gr.ianic;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

        Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizedRules = sessionManager.organizeRules(rules);

        organizedRules.forEach((tenant, erTuple) -> {
            System.out.println("Tenant: " + tenant);
            sessionManager.createStreamSession(erTuple.getKey(), tenant, erTuple.getValue());
            erTuple.getValue().forEach(rule -> System.out.println("    Rule: " + rule.getName()));
        });
    }


}
