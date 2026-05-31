package com.codetraininglab.catalog.api;

import com.codetraininglab.catalog.application.CustomTestsService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.CHALLENGE_CUSTOM_TESTS)
public class CustomTestsController {

  private final CustomTestsService customTestsService;

  public CustomTestsController(CustomTestsService customTestsService) {
    this.customTestsService = customTestsService;
  }

  @GetMapping
  CustomTestsService.CustomTestsResponse get(Authentication authentication, @PathVariable String slug) {
    return customTestsService.get(userId(authentication), slug);
  }

  @PutMapping
  CustomTestsService.CustomTestsResponse put(
      Authentication authentication,
      @PathVariable String slug,
      @RequestBody CustomTestsService.CustomTestsRequest request) {
    return customTestsService.save(userId(authentication), slug, request);
  }

  private static UUID userId(Authentication authentication) {
    return (UUID) authentication.getPrincipal();
  }
}
