package com.codetraininglab.identity.application;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AdminDashboardStatsResponse;
import com.codetraininglab.identity.api.AdminDashboardStatsResponse.AdminAccessRequestStats;
import com.codetraininglab.identity.api.AdminDashboardStatsResponse.AdminChallengeStats;
import com.codetraininglab.identity.api.AdminDashboardStatsResponse.AdminSubmissionStats;
import com.codetraininglab.identity.api.AdminDashboardStatsResponse.AdminUserStats;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

  private final UserRepository userRepository;
  private final AccessRequestRepository accessRequestRepository;
  private final ChallengeRepository challengeRepository;
  private final SubmissionRepository submissionRepository;

  public AdminDashboardService(
      UserRepository userRepository,
      AccessRequestRepository accessRequestRepository,
      ChallengeRepository challengeRepository,
      SubmissionRepository submissionRepository) {
    this.userRepository = userRepository;
    this.accessRequestRepository = accessRequestRepository;
    this.challengeRepository = challengeRepository;
    this.submissionRepository = submissionRepository;
  }

  public AdminDashboardStatsResponse stats() {
    long admins = userRepository.countByDeletedAtIsNullAndRole(UserRole.ADMIN);
    long learners = userRepository.countByDeletedAtIsNullAndRole(UserRole.USER);
    return new AdminDashboardStatsResponse(
        new AdminUserStats(admins + learners, admins, learners),
        new AdminAccessRequestStats(
            accessRequestRepository.countByStatus(AccessRequestStatus.PENDING),
            accessRequestRepository.countByStatus(AccessRequestStatus.APPROVED),
            accessRequestRepository.countByStatus(AccessRequestStatus.REJECTED)),
        new AdminChallengeStats(challengeRepository.count()),
        new AdminSubmissionStats(
            submissionRepository.count(),
            submissionRepository.countByStatus(SubmissionStatus.COMPLETED),
            submissionRepository.countByStatus(SubmissionStatus.FAILED),
            submissionRepository.countByStatus(SubmissionStatus.RUNNING),
            submissionRepository.countByStatus(SubmissionStatus.PENDING)));
  }
}
