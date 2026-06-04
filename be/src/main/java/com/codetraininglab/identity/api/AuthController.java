package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.AuthService;
import com.codetraininglab.identity.api.RegistrationInfoResponse;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/registration-info")
  RegistrationInfoResponse registrationInfo() {
    return authService.registrationInfo();
  }

  @PostMapping("/register")
  AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @GetMapping("/password-requirements")
  PasswordRequirementsResponse passwordRequirements() {
    return authService.passwordRequirements();
  }
}
