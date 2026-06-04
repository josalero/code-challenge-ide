package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.UserAdminService;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN_USERS)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserAdminService userAdminService;

  public AdminUserController(UserAdminService userAdminService) {
    this.userAdminService = userAdminService;
  }

  @PostMapping
  CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userAdminService.createUser(request);
  }
}
