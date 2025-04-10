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
 * A client for interacting with the Smartville Entities-v2 API.
 * <p>
 * This client handles OAuth2 authentication with the Smartville SSO service and provides methods
 * to fetch entity data and schema information from the API. It supports constructing Gremlin queries
 * for entity filtering and automatically manages access token lifecycle.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AuthConfig authConfig = new AuthConfig(...);
 * EntitiesClient client = new EntitiesClient("https://api.example.com", authConfig);
 *
 * // Get hydrometers for a project
 * Map<String, Object> query = EntitiesClient.createGremlinQuery("vrilissia_hydrometer");
 * List<JsonNode> hydrometers = client.getEntity("vrilissia", "OL2109", "hydrometer", query);
 * }
 * </pre>
 */
@ApplicationScoped
public class EntitiesClient {
    /**
     * Base URL of the Entities-v2 API (e.g., "<a href="https://v2-staging.data.smartville.gr/swagger-ui/#/">...</a>")
     */
    private final String baseUrl;

    /**
     * HTTP client instance for making requests
     */
    private final HttpClient httpClient;

    /**
     * Jackson ObjectMapper for JSON serialization/deserialization
     */
    private final ObjectMapper objectMapper;

    /**
     * Configuration for OAuth2 authentication
     */
    private final AuthConfig authConfig;

    /**
     * Current OAuth2 access token for API authorization
     */
    private String accessToken;

    /**
     * Constructs a new EntitiesClient with the specified configuration.
     *
     * @param baseUrl    the base URL of the Entities-v2 API, injected from configuration
     * @param authConfig authentication configuration containing client credentials
     */
    @Inject
    public EntitiesClient(
            @ConfigProperty(name = "entities.api.base-url") String baseUrl,
            AuthConfig authConfig) {
        this.baseUrl = baseUrl;
        this.authConfig = authConfig;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a standard Gremlin query structure for entity filtering.
     * <p>
     * The query will target entities with the specified label. Additional filters
     * can be added to the returned Map before use.
     * </p>
     *
     * @param entityLabel the fully qualified label of the target entity
     *                    (e.g., "vrilissia_hydrometer")
     * @return a Map representing the basic Gremlin query structure that can be
     * further customized
     */
    public static Map<String, Object> createGremlinQuery(String entityLabel) {
        return Map.of(
                "target_entity", Map.of(
                        "label", entityLabel,
                        "filters", List.of(
                                // Example filter (commented out):
                                /*Map.of(
                                    "property", "state",
                                    "operator", "eq",
                                    "values", List.of("active")
                                )*/
                        )
                ),
                "graph_filters", List.of()
        );
    }

    /**
     * Authenticates with the Smartville SSO service to obtain an access token.
     * <p>
     * Uses the OAuth2 password grant flow with client credentials. The obtained
     * access token is stored for subsequent API requests and automatically
     * refreshed when expired.
     * </p>
     *
     * @throws IOException          if there's an I/O error during the request or if
     *                              authentication fails
     * @throws InterruptedException if the operation is interrupted
     */
    public synchronized void authenticate() throws IOException, InterruptedException {
        // Prepare URL-encoded form data for token request
        String formData = String.format(
                "grant_type=password&username=%s&password=%s&client_id=%s&client_secret=%s",
                URLEncoder.encode(authConfig.username, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.password, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(authConfig.clientSecret, StandardCharsets.UTF_8)
        );

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authConfig.authUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        // Execute request and handle response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            try {
                // Parse JSON response to extract access token
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
            throw new IOException(String.format(
                    "Authentication failed with status %d: %s",
                    response.statusCode(),
                    response.body())
            );
        }
    }

    /**
     * Makes an authenticated request to the Entities-v2 API.
     * <p>
     * Handles automatic authentication and token refresh. The Gremlin query is
     * automatically converted to JSON and URL-encoded for the request.
     * </p>
     *
     * @param tenant       the tenant identifier (e.g., "vrilissia")
     * @param project      the project identifier (e.g., "OL2109")
     * @param entity       the entity type to query (e.g., "hydrometer")
     * @param gremlinQuery the Gremlin query parameters as a Map structure
     * @return the raw JSON response from the API
     * @throws IOException          if there's an I/O error or the request fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String makeRequest(
            String tenant,
            String project,
            String entity,
            Map<String, Object> gremlinQuery
    ) throws IOException, InterruptedException {
        // Ensure we have a valid access token
        if (accessToken == null) {
            authenticate();
        }

        // Convert query to JSON and URL encode
        String gremlinJson = objectMapper.writeValueAsString(gremlinQuery);
        String encodedGremlin = URLEncoder.encode(gremlinJson, StandardCharsets.UTF_8);

        // Build request URL
        String url = String.format(
                "%s/balloon-works/%s/%s/%s?gremlin_query=%s",
                baseUrl,
                tenant,
                project,
                entity,
                encodedGremlin
        );

        // Create and send request
        HttpRequest request = buildAuthorizedRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle token expiration (retry once with new token)
        if (response.statusCode() == 401) {
            authenticate();
            request = buildAuthorizedRequest(url);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        // Check for successful response
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException(String.format(
                    "API request failed with status %d: %s",
                    response.statusCode(),
                    response.body())
            );
        }
    }

    /**
     * Builds an authorized HTTP GET request with the current access token.
     *
     * @param url the target URL for the request
     * @return a configured HttpRequest with authorization headers
     */
    private HttpRequest buildAuthorizedRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
    }

    /**
     * Fetches the schema definition for a tenant's entity types.
     *
     * @param tenant the tenant identifier (e.g., "vrilissia")
     * @return the raw schema JSON as a string
     * @throws IOException          if there's an I/O error or the request fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String fetchSchemaJson(String tenant) throws IOException, InterruptedException {
        if (accessToken == null) {
            authenticate();
        }

        String schemaUrl = String.format("%s/schema/%s/balloon-works/entityTypes", baseUrl, tenant);
        HttpRequest request = buildAuthorizedRequest(schemaUrl);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            authenticate();
            request = buildAuthorizedRequest(schemaUrl);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException(String.format(
                    "Schema request failed with status %d: %s",
                    response.statusCode(),
                    response.body())
            );
        }
    }

    /**
     * Retrieves entities of a specific type with schema-aware mapping.
     * <p>
     * Combines fetching the schema definition with entity data to provide
     * structured results that respect the tenant's schema.
     * </p>
     *
     * @param tenant       the tenant identifier
     * @param project      the project identifier
     * @param entity       the entity type to retrieve
     * @return a list of JsonNode objects representing the mapped entities
     * @throws Exception if any step of the process fails
     */
    public List<JsonNode> getEntity(String tenant, String project, String entity) throws Exception {
        Map<String, Object> gremlinQuery = createGremlinQuery(tenant + "_" + entity);

        // 1. Fetch current schema definition
        String schemaJson = fetchSchemaJson(tenant);

        // 2. Get raw entity data
        String rawJsonResponse = makeRequest(tenant, project, entity, gremlinQuery);

        // 3. Parse schema to understand entity structure
        Map<String, Map<String, String>> schemaMap = SchemaParser.parseSchema(schemaJson);

        // 4. Map raw results to schema-defined structure
        return SchemaParser.mapResultsToSchema(rawJsonResponse, entity, schemaMap);
    }
}