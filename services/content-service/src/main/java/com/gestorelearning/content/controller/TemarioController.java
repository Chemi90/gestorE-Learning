package com.gestorelearning.content.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/temarios")
public class TemarioController {

    @PostMapping("/test")
    public Map<String, String> testProtected() {
        return Map.of("message", "ok protegido");
    }
}
