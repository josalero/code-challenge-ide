package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.AccessRequestAdminService;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN_ACCESS_REQUESTS)
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccessRequestController {

  private final AccessRequestAdminService accessRequestAdminService;

  public AdminAccessRequestController(AccessRequestAdminService accessRequestAdminService) {
    this.accessRequestAdminService = accessRequestAdminService;
  }

  @GetMapping
  List<AccessRequestSummary> list(@RequestParam(required = false) String status) {
    return accessRequestAdminService.list(status);
  }

  @GetMapping("/pending-count")
  PendingAccessRequestCountResponse pendingCount() {
    return new PendingAccessRequestCountResponse(accessRequestAdminService.countPending());
  }

  @PostMapping("/{id}/reject")
  AccessRequestSummary reject(
      @PathVariable UUID id,
      Authentication authentication,
      @Valid @RequestBody(required = false) RejectAccessRequestRequest request) {
    RejectAccessRequestRequest body =
        request == null ? new RejectAccessRequestRequest(null) : request;
    return accessRequestAdminService.reject(id, adminUserId(authentication), body);
  }

  @PostMapping("/{id}/approve")
  CreateUserResponse approve(
      @PathVariable UUID id,
      Authentication authentication,
      @Valid @RequestBody ApproveAccessRequestRequest request) {
    return accessRequestAdminService.approve(id, adminUserId(authentication), request);
  }

  private static UUID adminUserId(Authentication authentication) {
    return (UUID) authentication.getPrincipal();
  }
}
