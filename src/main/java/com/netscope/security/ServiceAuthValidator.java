package com.netscope.security;

import com.netscope.core.NetScopeSecurityConfig;
import com.netscope.model.NetworkMethodDefinition;

/**
 * Validates API keys for restricted methods.
 */
public class ServiceAuthValidator {

    private final NetScopeSecurityConfig securityConfig;

    public ServiceAuthValidator(NetScopeSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Validate API key for a method call.
     * 
     * @param definition The method definition
     * @param providedKey The API key provided by the client
     * @return true if validation passes
     */
    public boolean validate(NetworkMethodDefinition definition, String providedKey) {
        // Public methods don't need validation
        if (!definition.isRestricted()) {
            return true;
        }

        // If security is not enabled, allow access
        if (!securityConfig.isEnabled()) {
            return true;
        }

        // Check method-specific key first
        String methodKey = definition.getApiKey();
        if (!methodKey.isEmpty()) {
            return methodKey.equals(providedKey);
        }

        // Fall back to global key
        String globalKey = securityConfig.getApiKey();
        return globalKey != null && globalKey.equals(providedKey);
    }

    /**
     * Get the expected key for error messages (without revealing the actual key).
     */
    public String getExpectedKeyHint(NetworkMethodDefinition definition) {
        if (!definition.isRestricted()) {
            return "No key required (public method)";
        }

        String methodKey = definition.getApiKey();
        if (!methodKey.isEmpty()) {
            return "Method-specific key required";
        }

        return "Global API key required";
    }
}
