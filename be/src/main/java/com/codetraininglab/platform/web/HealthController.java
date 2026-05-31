package com.codetraininglab.platform.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.HEALTH)
public class HealthController {

  @GetMapping
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }
}
