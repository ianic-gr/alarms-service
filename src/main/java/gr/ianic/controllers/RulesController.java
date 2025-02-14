package gr.ianic.controllers;

import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionFactory;
import gr.ianic.rules.StreamSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;


@Singleton
@Path("/rules")
public class RulesController {
    @Inject
    StreamSession ruleStream;

    @Inject
    RulesDao rulesDao;

    @Inject
    SessionFactory sessionFactory;

    @PUT
    @Path("/reload/{tenant}/{source}")
    public Response reloadRules(@PathParam("tenant") String tenant, @PathParam("source") String source) {
        sessionFactory.getStreamSession(tenant, source).reloadRules();
        return Response.ok("Rules reloaded successfully").build();
    }

    @POST
    @Path("/{tenant}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRules(@PathParam("tenant") String tenant, @RequestBody Rule rule) {
        rule.setTenant(tenant);
        rulesDao.instert(rule);
        return Response.ok("Rules added successfully").build();
    }
}
