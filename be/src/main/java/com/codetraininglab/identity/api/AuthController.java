package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.AccessRequestService;
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
  private final AccessRequestService accessRequestService;

  public AuthController(AuthService authService, AccessRequestService accessRequestService) {
    this.authService = authService;
    this.accessRequestService = accessRequestService;
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

  @PostMapping("/access-request")
  AccessRequestResponse submitAccessRequest(@Valid @RequestBody AccessRequest request) {
    return accessRequestService.submit(request);
  }

  @GetMapping("/password-requirements")
  PasswordRequirementsResponse passwordRequirements() {
    return authService.passwordRequirements();
  }
}
