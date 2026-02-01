package com.netscope.core;

import com.netscope.model.NetworkMethodDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RestController
public class NetScopeDocController {

    private final NetScopeScanner scanner;

    public NetScopeDocController(NetScopeScanner scanner) {
        this.scanner = scanner;
    }

    @GetMapping("/netscope/docs")
    public List<NetworkMethodDefinition> getDocs() {
        // scan user beans lazily here if you want
        return scanner.scan();
    }

    @GetMapping("/netscope/docs/ui")
    public String docsUi() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/netscope-docs-ui.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}


