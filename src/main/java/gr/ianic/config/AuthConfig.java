package gr.ianic.config;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ConfigProperties(prefix = "smartville")
public class AuthConfig {

    @ConfigProperty(name = "api.base-url")
    public String baseUrl;

    @ConfigProperty(name = "auth.url")
    public String authUrl;

    @ConfigProperty(name = "auth.client-id")
    public String clientId;

    @ConfigProperty(name = "auth.client-secret")
    public String clientSecret;

    @ConfigProperty(name = "auth.username")
    public String username;

    @ConfigProperty(name = "auth.password")
    public String password;
}