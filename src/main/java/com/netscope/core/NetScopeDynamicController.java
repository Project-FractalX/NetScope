package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import com.netscope.security.ServiceAuthValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Dynamic REST controller that handles all network-exposed method calls.
 */
@RestController
@RequestMapping("/netscope")
public class NetScopeDynamicController {

    private final NetScopeScanner scanner;
    private final NetScopeInvoker invoker;
    private final ServiceAuthValidator authValidator;
    private final List<NetworkMethodDefinition> exposedMethods;

    public NetScopeDynamicController(NetScopeScanner scanner, 
                                     NetScopeSecurityConfig securityConfig) {
        this.scanner = scanner;
        this.invoker = new NetScopeInvoker();
        this.authValidator = new ServiceAuthValidator(securityConfig);
        this.exposedMethods = scanner.scan();
    }

    /**
     * Handle all dynamic method invocations.
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> handleRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        try {
            String path = request.getRequestURI();
            
            // Find matching method
            Optional<NetworkMethodDefinition> methodOpt = exposedMethods.stream()
                .filter(m -> m.getPath().equals(path) && m.isRestEnabled())
                .findFirst();

            if (methodOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Method not found: " + path);
            }

            NetworkMethodDefinition method = methodOpt.get();

            // Validate authentication
            if (!authValidator.validate(method, apiKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed. " + authValidator.getExpectedKeyHint(method));
            }

            // Invoke method
            Object result = invoker.invoke(method, body);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}
