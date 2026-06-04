package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.AdminUserChallengeDetailService;
import com.codetraininglab.identity.application.AdminUserChallengeReportService;
import com.codetraininglab.identity.application.UserAdminService;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN_USERS)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserAdminService userAdminService;
  private final AdminUserChallengeReportService challengeReportService;
  private final AdminUserChallengeDetailService challengeDetailService;

  public AdminUserController(
      UserAdminService userAdminService,
      AdminUserChallengeReportService challengeReportService,
      AdminUserChallengeDetailService challengeDetailService) {
    this.userAdminService = userAdminService;
    this.challengeReportService = challengeReportService;
    this.challengeDetailService = challengeDetailService;
  }

  @GetMapping
  List<AdminUserSummary> listUsers(
      @RequestParam(defaultValue = "false") boolean includeInactive) {
    return userAdminService.listUsers(includeInactive);
  }

  @PostMapping
  CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userAdminService.createUser(request);
  }

  @GetMapping("/{userId}/challenge-report")
  AdminUserChallengeReportResponse challengeReport(@PathVariable UUID userId) {
    return challengeReportService.reportForUser(userId);
  }

  @GetMapping("/{userId}/challenges/{challengeSlug}/detail")
  AdminUserChallengeDetailResponse challengeDetail(
      @PathVariable UUID userId, @PathVariable String challengeSlug) {
    return challengeDetailService.detailForUserChallenge(userId, challengeSlug);
  }

  @PostMapping("/{userId}/deactivate")
  void deactivateUser(@PathVariable UUID userId, Authentication authentication) {
    UUID actorId = (UUID) authentication.getPrincipal();
    userAdminService.deactivateUser(actorId, userId);
  }

  @GetMapping("/{userId}/challenge-quota")
  UserChallengeQuotaResponse challengeQuota(@PathVariable UUID userId) {
    return userAdminService.challengeQuota(userId);
  }

  @PatchMapping("/{userId}/challenge-quota")
  UserChallengeQuotaResponse updateChallengeQuota(
      @PathVariable UUID userId, @Valid @RequestBody UpdateUserChallengeQuotaRequest request) {
    return userAdminService.updateChallengeQuota(userId, request);
  }
}
