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

    //@Inject
    //SessionFactory sessionFactory;

    @Inject
    RulesDao rulesDao;

    List<Rule> rules;

    @PostConstruct
    public void init() {
        rules = rulesDao.getByType("stream").all();

        // Grouping rules by tenant and then by source
        Map<String, Map<String, List<Rule>>> groupedRules = rules.stream()
                .collect(Collectors.groupingBy(Rule::getTenant,
                        Collectors.groupingBy(Rule::getSource)));

        // Now we can access rules like groupedRules.get("tenant1").get("source1")
        groupedRules.forEach((tenant, sourceMap) -> {
            System.out.println("Tenant: " + tenant);
            sourceMap.forEach((source, ruleList) -> {
                System.out.println("  Source: " + source);
                ruleList.forEach(rule -> System.out.println("    Rule: " + rule.getName()));
            });
        });
    }

}
