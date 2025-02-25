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
}
