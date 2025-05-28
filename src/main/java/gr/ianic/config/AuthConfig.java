package gr.ianic.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthConfig {

    @ConfigProperty(name = "smartville.auth.url")
    String authUrl;

    @ConfigProperty(name = "smartville.auth.client-id")
    String clientId;

    @ConfigProperty(name = "smartville.auth.client-secret")
    String clientSecret;

    @ConfigProperty(name = "smartville.auth.username")
    String username;

    @ConfigProperty(name = "smartville.auth.password")
    String password;

    // Getters (optional)
    public String getAuthUrl() { return authUrl; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
