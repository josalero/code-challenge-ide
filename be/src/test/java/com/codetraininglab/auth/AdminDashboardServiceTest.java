package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AccessRequestRepository accessRequestRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private SubmissionRepository submissionRepository;

  @InjectMocks private AdminDashboardService adminDashboardService;

  @Test
  void statsAggregatesPlatformCounts() {
    when(userRepository.countByDeletedAtIsNullAndRole(UserRole.ADMIN)).thenReturn(2L);
    when(userRepository.countByDeletedAtIsNullAndRole(UserRole.USER)).thenReturn(15L);
    when(accessRequestRepository.countByStatus(AccessRequestStatus.PENDING)).thenReturn(3L);
    when(accessRequestRepository.countByStatus(AccessRequestStatus.APPROVED)).thenReturn(8L);
    when(accessRequestRepository.countByStatus(AccessRequestStatus.REJECTED)).thenReturn(1L);
    when(challengeRepository.count()).thenReturn(42L);
    when(submissionRepository.count()).thenReturn(120L);
    when(submissionRepository.countByStatus(SubmissionStatus.COMPLETED)).thenReturn(90L);
    when(submissionRepository.countByStatus(SubmissionStatus.FAILED)).thenReturn(10L);
    when(submissionRepository.countByStatus(SubmissionStatus.RUNNING)).thenReturn(2L);
    when(submissionRepository.countByStatus(SubmissionStatus.PENDING)).thenReturn(5L);

    var stats = adminDashboardService.stats();

    assertThat(stats.users().total()).isEqualTo(17);
    assertThat(stats.users().admins()).isEqualTo(2);
    assertThat(stats.users().learners()).isEqualTo(15);
    assertThat(stats.accessRequests().pending()).isEqualTo(3);
    assertThat(stats.challenges().total()).isEqualTo(42);
    assertThat(stats.submissions().completed()).isEqualTo(90);
  }
}
