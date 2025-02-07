package gr.ianic.controllers;

import gr.ianic.SimpleRuleStream;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Singleton
@Path("/rules")
public class RulesController {
    @Inject
    SimpleRuleStream ruleStream;

    @PUT
    @Path("/reload/{tenant}")
    public Response reloadRules(@PathParam("tenant") String tenant) {
        ruleStream.reloadRules(tenant);
        return Response.ok("Rules reloaded successfully").build();
    }
}
