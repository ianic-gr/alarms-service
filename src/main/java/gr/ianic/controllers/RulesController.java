package gr.ianic.controllers;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;


@ApplicationScoped
@Path("/rules")
public class RulesController {

    @Inject
    RulesDao rulesDao;

    @Inject
    SessionManager sessionManager;

    @PUT
    @Path("/reload/{tenant}")
    public Response reloadRules(@PathParam("tenant") String tenant) {
        sessionManager.reloadRulesForStreamSession(tenant);
        return Response.ok("Rules reloaded successfully").build();
    }

    @POST
    @Path("/{tenant}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRules(@PathParam("tenant") String tenant, @RequestBody Rule rule) {
        rule.setTenant(tenant);
        rulesDao.insert(rule);
        return Response.ok("Rules added successfully").build();
    }

    @POST
    @Path("/session/{tenant}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession(@PathParam("tenant") String tenant, @QueryParam("mode") String mode) {
        if (mode == null || mode.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter 'mode' is required")
                    .build();
        }

        List<Rule> rules = rulesDao.getByTenantAndMode(tenant, mode).all();

        if (rules.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No rules found for tenant " + tenant + " and mode " + mode)
                    .build();
        }

        if (mode.equalsIgnoreCase("stream")) {
            AbstractMap.SimpleEntry<Set<String>, List<Rule>> organizedRules = sessionManager.organizeSingleTenantRules(rules);

            boolean sessionCreated = sessionManager.createStreamSession(organizedRules.getKey(), tenant, organizedRules.getValue());

            if (sessionCreated) {
                return Response.ok("Session created successfully for tenant: " + tenant + " with mode: " + mode).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to create session for tenant: " + tenant)
                        .build();
            }
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid mode").build();
    }

}
