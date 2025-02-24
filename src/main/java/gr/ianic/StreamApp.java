package gr.ianic;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionFactory;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Startup
@ApplicationScoped
public class StreamApp {

    @Inject
    SessionFactory sessionFactory;

    @Inject
    RulesDao rulesDao;

    List<Rule> rules;

    @PostConstruct
    public void init() {
        rules = rulesDao.getByMode("stream").all();

        // Grouping rules by tenant and then by source
       Map<String, List<Rule>> groupedRules = rules.stream()
                .collect(Collectors.groupingBy(Rule::getTenant));

        // Now we can access rules like groupedRules.get("tenant1").get("source1")
        groupedRules.forEach((tenant, ruleList) -> {
            System.out.println("Tenant: " + tenant);
                sessionFactory.createStreamSession(source, tenant, ruleList);
                ruleList.forEach(rule -> System.out.println("    Rule: " + rule.getName()));
        });
    }

}
