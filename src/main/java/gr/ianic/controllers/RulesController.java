package gr.ianic.controllers;

import gr.ianic.model.rules.Rule;
import gr.ianic.model.WaterMeter; // Import the WaterMeter class
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.rules.SessionManager;
import gr.ianic.rules.TenantRulesInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

/**
 * REST controller for managing rules and sessions.
 * This class provides endpoints for reloading rules, adding rules, and creating sessions for tenants.
 */
@ApplicationScoped
@Path("/rules")
public class RulesController {

    @Inject
    RulesDao rulesDao; // Data access object for interacting with rules in the database

    @Inject
    SessionManager sessionManager; // Manages stream and scheduled sessions

    /**
     * Reloads the rules for a stream session associated with the given tenant.
     *
     * @param tenant The tenant identifier for which to reload the rules.
     * @return A response indicating the success or failure of the operation.
     */
    @PUT
    @Path("/reload/{tenant}")
    public Response reloadRules(@PathParam("tenant") String tenant) {
        sessionManager.reloadRulesForStreamSession(tenant);
        return Response.ok("Rules reloaded successfully").build();
    }

    /**
     * Adds a new rule for the specified tenant.
     *
     * @param tenant The tenant identifier for which to add the rule.
     * @param rule   The rule to add.
     * @return A response indicating the success or failure of the operation.
     */
    @POST
    @Path("/{tenant}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRules(@PathParam("tenant") String tenant, @RequestBody Rule rule) {
        rule.setTenant(tenant);
        rulesDao.insert(rule);
        return Response.ok("Rule added successfully").build();
    }

    /**
     * Creates a new session for the specified tenant and mode.
     *
     * @param tenant The tenant identifier for which to create the session.
     * @param mode   The mode of the session (e.g., "stream"). Defaults to "stream" if not provided.
     * @return A response indicating the success or failure of the operation.
     */
    @POST
    @Path("/session/{tenant}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession(
            @PathParam("tenant") String tenant,
            @QueryParam("mode") @DefaultValue("stream") String mode // Default value is "stream"
    ) {
        // Fetch rules for the tenant and mode
        List<Rule> rules = rulesDao.getByTenantAndMode(tenant, mode).all();

        // Check if rules exist for the tenant and mode
        if (rules.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No rules found for tenant " + tenant + " and mode " + mode)
                    .build();
        }

        // Handle stream mode
        if (mode.equalsIgnoreCase("stream")) {
            // Organize rules by entry points
            @NotNull TenantRulesInfo tenantRulesInfo = sessionManager.organizeSingleTenantRules(rules);

            // Create a stream session
            boolean sessionCreated = sessionManager.createStreamSession(tenant, tenantRulesInfo);

            if (sessionCreated) {
                return Response.ok("Session created successfully for tenant: " + tenant + " with mode: " + mode).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to create session for tenant: " + tenant)
                        .build();
            }
        }

        // Handle invalid mode
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid mode").build();
    }

    /**
     * Updates a WaterMeter fact in the session for the specified tenant.
     *
     * @param tenant     The tenant identifier for which to update the WaterMeter.
     * @param waterMeter The updated WaterMeter object.
     * @return A response indicating the success or failure of the operation.
     */
    @PUT
    @Path("/watermeter/{tenant}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWaterMeter(@PathParam("tenant") String tenant, @RequestBody WaterMeter waterMeter) {
        // Use the SessionManager to update the WaterMeter fact
        boolean isUpdated = sessionManager.updateWaterMetersFact(tenant, waterMeter);

        if (isUpdated) {
            return Response.ok("WaterMeter facts updated successfully for tenant: " + tenant).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Session not found for tenant: " + tenant)
                    .build();
        }
    }
}