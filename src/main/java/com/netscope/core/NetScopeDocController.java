package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for NetScope documentation.
 */
@RestController
@RequestMapping("/netscope/docs")
public class NetScopeDocController {

    private final NetScopeScanner scanner;

    public NetScopeDocController(NetScopeScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Get JSON documentation of all exposed methods.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NetworkMethodDefinition> getDocs() {
        return scanner.scan().stream()
            .filter(NetworkMethodDefinition::isRestEnabled)
            .collect(Collectors.toList());
    }

    /**
     * Serve the interactive documentation UI.
     */
    @GetMapping(value = "/ui", produces = MediaType.TEXT_HTML_VALUE)
    public String getDocsUi() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/netscope-docs-ui.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
