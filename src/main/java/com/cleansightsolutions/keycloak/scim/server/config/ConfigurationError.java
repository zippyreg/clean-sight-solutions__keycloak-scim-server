package com.cleansightsolutions.keycloak.scim.server.config;

/**
 * Configuration error
 */
public class ConfigurationError extends Exception {

    public ConfigurationError(String message) {
        super(message);
    }

}
