package com.codetraininglab.identity.api;

import com.codetraininglab.identity.application.AdminDashboardService;
import com.codetraininglab.platform.web.ApiPaths;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN_DASHBOARD)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  public AdminDashboardController(AdminDashboardService adminDashboardService) {
    this.adminDashboardService = adminDashboardService;
  }

  @GetMapping
  AdminDashboardStatsResponse dashboard() {
    return adminDashboardService.stats();
  }
}
