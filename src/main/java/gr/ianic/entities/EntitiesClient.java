package gr.ianic.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.config.AuthConfig;
import jakarta.inject.Inject;

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
public class EntitiesClient {
    /**
     * Base URL of the Entities-v2 API (e.g., "<a href="https://v2-staging.data.smartville.gr/swagger-ui/#/">...</a>")
     */
    private final String baseUrl;

    /**
     * Tenant identifier (e.g., "vrilissia")
     */
    private final String tenant;

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
    @Inject
    AuthConfig authConfig;

    /**
     * Current OAuth2 access token
     */
    private String accessToken;

    /**
     * Constructs a new EntitiesClient with the specified configuration.
     *
     * @param baseUrl    the base URL of the Smartville API
     * @param tenant     the tenant identifier
     * @param authConfig authentication configuration
     */
    public EntitiesClient(String baseUrl, String tenant, AuthConfig authConfig) {
        this.baseUrl = baseUrl;
        this.tenant = tenant;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authConfig = authConfig;
    }

    /**
     * Creates a standard Gremlin query for entity filtering.
     *
     * @param entityLabel the label of the target entity (e.g., "vrilissia_hydrometer")
     * @param state       the state to filter by (e.g., "active")
     * @return a Map representing the Gremlin query structure
     */
    public static Map<String, Object> createGremlinQuery(String entityLabel, String state) {
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
    public String makeRequest(String project, String entity, Map<String, Object> gremlinQuery)
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

}