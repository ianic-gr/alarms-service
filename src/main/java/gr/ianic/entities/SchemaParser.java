package gr.ianic.entities;

import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A utility class for parsing JSON schemas and mapping raw JSON results to schema-defined structures.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Parse entity definitions from a JSON schema</li>
 *   <li>Map raw query results to conform to a schema's structure and data types</li>
 * </ul>
 * </p>
 */
public class SchemaParser {
    // Jackson ObjectMapper for JSON processing
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a JSON schema definition and extracts entity-field mappings.
     *
     * @param jsonString The JSON string containing schema definitions
     * @return A nested map where:
     *         - Outer key: Entity name (e.g., "vrilissia_gateway")
     *         - Inner map: Field names mapped to their data types (e.g., "guid" → "UUID")
     * @throws org.json.JSONException If the input JSON is malformed
     */
    public static @NotNull Map<String, Map<String, String>> parseSchema(String jsonString) {
        Map<String, Map<String, String>> entityFields = new HashMap<>();

        // Parse the root JSON object
        JSONObject json = new JSONObject(jsonString);
        if (!json.has("definitions")) {
            return entityFields;  // Return empty map if no definitions exist
        }

        // Process each entity definition in the "definitions" array
        JSONArray definitions = json.getJSONArray("definitions");
        for (int i = 0; i < definitions.length(); i++) {
            JSONObject entity = definitions.getJSONObject(i);
            String entityName = entity.getString("entityName");

            if (entity.has("properties")) {
                Map<String, String> fields = new HashMap<>();
                JSONArray properties = entity.getJSONArray("properties");

                // Extract all properties and their data types
                for (int j = 0; j < properties.length(); j++) {
                    JSONObject field = properties.getJSONObject(j);
                    String fieldName = field.getString("propertyName");
                    // Use "unknown" as default if dataType is missing
                    String dataType = field.optString("dataType", "unknown");
                    fields.put(fieldName, dataType);
                }

                entityFields.put(entityName, fields);
            }
        }
        return entityFields;
    }

    /**
     * Maps raw JSON results to conform to a schema's structure and data types.
     *
     * @param resultsJson The JSON string containing raw results (must have "results" array)
     * @param entity      The target entity type (e.g., "vrilissia_gateway")
     * @param schemaMap   The schema map from parseSchema()
     * @return A list of JsonNodes where each node contains only schema-defined fields
     *         with proper type conversion
     * @throws Exception If JSON parsing fails or required fields are missing
     */
    public static @NotNull List<JsonNode> mapResultsToSchema(
            String resultsJson,
            String entity,
            @NotNull Map<String, Map<String, String>> schemaMap
    ) throws Exception {
        JsonNode rootNode = objectMapper.readTree(resultsJson);
        List<JsonNode> mappedResults = new ArrayList<>();

        // Return empty list if entity not found in schema
        if (!schemaMap.containsKey(entity)) {
            return mappedResults;
        }

        Map<String, String> fieldTypes = schemaMap.get(entity);

        // Process each entity in the "results" array
        if (rootNode.has("results") && rootNode.get("results").isArray()) {
            for (JsonNode entityNode : rootNode.get("results")) {
                ObjectNode mappedNode = objectMapper.createObjectNode();

                // Convert each field according to schema type
                for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
                    String field = entry.getKey();
                    String type = entry.getValue();

                    if (entityNode.has(field) && !entityNode.get(field).isNull()) {
                        switch (type.toLowerCase(Locale.ROOT)) {
                            case "integer":
                            case "int":
                                mappedNode.put(field, entityNode.get(field).asInt());
                                break;
                            case "double":
                                mappedNode.put(field, entityNode.get(field).asDouble());
                                break;
                            case "boolean":
                                mappedNode.put(field, entityNode.get(field).asBoolean());
                                break;
                            default:  // Applies to String, UUID, Date, etc.
                                mappedNode.put(field, entityNode.get(field).asText());
                        }
                    }
                }
                mappedResults.add(mappedNode);
            }
        }
        return mappedResults;
    }

}