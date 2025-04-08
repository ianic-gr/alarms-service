package gr.ianic.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.config.AuthConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A client for interacting with the Entities-v2 API.
 * <p>
 * This client handles authentication with the Smartville SSO service and makes
 * authorized requests to retrieve entity data from the API. It supports constructing
 * Gremlin queries and automatically manages OAuth2 access tokens.
 * </p>
 */
@ApplicationScoped
public class EntitiesClient {
    /**
     * Base URL of the Entities-v2 API (e.g., "<a href="https://v2-staging.data.smartville.gr/swagger-ui/#/">...</a>")
     */
    private final String baseUrl;

    /**
     * HTTP client for making requests
     */
    private final HttpClient httpClient;

    /**
     * Jackson ObjectMapper for JSON serialization/deserialization
     */
    private final ObjectMapper objectMapper;

    /**
     * Authentication configuration
     */
    AuthConfig authConfig;

    /**
     * Current OAuth2 access token
     */
    private String accessToken;

    /**
     * Constructs a new EntitiesClient with the specified configuration.
     *
     * @param baseUrl the base URL of the Entities-v2 API
     */
    @Inject
    public EntitiesClient(@ConfigProperty(name = "entities.api.base-url") String baseUrl, AuthConfig authConfig) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.authConfig = authConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a standard Gremlin query for entity filtering.
     *
     * @param entityLabel the label of the target entity (e.g., "vrilissia_hydrometer")
     * @return a Map representing the Gremlin query structure
     */
    public static Map<String, Object> createGremlinQuery(String entityLabel) {
        return Map.of(
                "target_entity", Map.of(
                        "label", entityLabel,
                        "filters", List.of(
                                /*Map.of(
                                        "property", "state",
                                        "operator", "eq",
                                        "values", List.of(state)
                                )*/
                        )
                ),
                "graph_filters", List.of()
        );
    }


    /**
     * Authenticates with the Smartville SSO service to obtain an access token.
     * <p>
     * Uses the password grant type with client credentials. The obtained access token
     * is stored for subsequent API requests.
     * </p>
     *
     * @throws IOException          if there's an I/O error or authentication fails
     * @throws InterruptedException if the operation is interrupted
     */
    public synchronized void authenticate() throws IOException, InterruptedException {
        // Prepare form data for OAuth2 token request
        String formData = String.format(
                "grant_type=password&username=%s&password=%s&client_id=%s&client_secret=%s",
                URLEncoder.encode(authConfig.username, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.password, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.clientSecret, StandardCharsets.UTF_8)
        );

        // Build the authentication request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authConfig.authUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        // Execute the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            try {
                // Parse the JSON response to extract the access token
                Map<String, Object> authResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<>() {
                        }
                );
                this.accessToken = (String) authResponse.get("access_token");
            } catch (Exception e) {
                throw new IOException("Failed to parse authentication response", e);
            }
        } else {
            throw new IOException("Authentication failed with status code: " + response.statusCode() +
                    ", response: " + response.body());
        }
    }

    /**
     * Makes a request to the Smartville Balloon-Works API.
     * <p>
     * Automatically handles authentication if no token is available and retries once
     * if the token has expired. The Gremlin query is automatically converted to JSON
     * and URL-encoded.
     * </p>
     *
     * @param project      the project identifier (e.g., "OL2109")
     * @param entity       the entity type (e.g., "hydrometer")
     * @param gremlinQuery the Gremlin query as a Map structure
     * @return the API response as a JSON string
     * @throws IOException          if there's an I/O error or the request fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String makeRequest(String tenant, String project, String entity, Map<String, Object> gremlinQuery)
            throws IOException, InterruptedException {
        // Authenticate if we don't have a token
        if (accessToken == null) {
            authenticate();
        }

        // Convert gremlin query to JSON and URL encode it
        String gremlinJson = objectMapper.writeValueAsString(gremlinQuery);
        String encodedGremlin = URLEncoder.encode(gremlinJson, StandardCharsets.UTF_8);

        // Build the request URL
        String url = String.format("%s/balloon-works/%s/%s/%s?gremlin_query=%s",
                baseUrl,
                tenant,
                project,
                entity,
                encodedGremlin);

        // Create and send the request with authorization header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            // Token might be expired, try to reauthenticate once
            authenticate();
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("Request failed with status code: " + response.statusCode() +
                    ", response: " + response.body());
        }
    }

    /**
     * Fetches the schema JSON from the Entities-v2 API for a specific tenant.
     *
     * @param tenant the tenant identifier (e.g., "vrilissia")
     * @return the raw schema JSON as a string
     * @throws IOException          if there's an I/O error or the request fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String fetchSchemaJson(String tenant) throws IOException, InterruptedException {
        // Authenticate if token is missing
        if (accessToken == null) {
            authenticate();
        }

        String schemaUrl = String.format("%s/schema/%s/balloon-works/entityTypes", baseUrl, tenant);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(schemaUrl))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            // Token expired? Re-auth and retry once
            authenticate();
            request = HttpRequest.newBuilder()
                    .uri(URI.create(schemaUrl))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("Schema request failed with status code: " + response.statusCode() +
                    ", response: " + response.body());
        }
    }


    public List<JsonNode> getEntity(
            String tenant,
            String project,
            String entity,
            Map<String, Object> gremlinQuery
    ) throws Exception {
        // Fetch schema JSON using the tenant
        String schemaJson = fetchSchemaJson(tenant);

        // Call the API and get raw JSON response
        String rawJsonResponse = makeRequest(tenant, project, entity, gremlinQuery);

        // Parse the schema to extract mapping info
        Map<String, Map<String, String>> schemaMap = SchemaParser.parseSchema(schemaJson);

        // Map the raw results to schema-defined structure
        return SchemaParser.mapResultsToSchema(rawJsonResponse, entity, schemaMap);
    }


}