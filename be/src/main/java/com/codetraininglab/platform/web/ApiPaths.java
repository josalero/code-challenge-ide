package com.codetraininglab.platform.web;

public final class ApiPaths {

  public static final String BASE = "/api/v1";
  public static final String HEALTH = BASE + "/health";
  public static final String AUTH = BASE + "/auth";
  public static final String AUTH_LOGIN = AUTH + "/login";
  public static final String AUTH_REGISTER = AUTH + "/register";
  public static final String AUTH_REGISTRATION_INFO = AUTH + "/registration-info";
  public static final String AUTH_ACCESS_REQUEST = AUTH + "/access-request";
  public static final String LANGUAGES = BASE + "/languages";
  public static final String CHALLENGES = BASE + "/challenges";
  public static final String CHALLENGE_CUSTOM_TESTS = CHALLENGES + "/{slug}/custom-tests";
  public static final String ME = BASE + "/me";
  public static final String ME_PROGRESS = ME + "/progress";
  public static final String ME_METRICS = ME + "/metrics";
  public static final String ME_PASSWORD = ME + "/password";
  public static final String AUTH_PASSWORD_REQUIREMENTS = AUTH + "/password-requirements";
  public static final String ADMIN_USERS = BASE + "/admin/users";
  public static final String ADMIN_DASHBOARD = BASE + "/admin/dashboard";
  public static final String ADMIN_ACCESS_REQUESTS = BASE + "/admin/access-requests";
  public static final String SUBMISSIONS = BASE + "/submissions";
  public static final String SUBMISSION_FEEDBACK_ACTIONS = SUBMISSIONS + "/{submissionId}/feedback-actions";
  public static final String FEEDBACK_ACTIONS = BASE + "/feedback-actions";
  public static final String LSP_PREFIX = BASE + "/lsp/";
  public static final String LSP_JAVA = LSP_PREFIX + "java";

  public static String lsp(String language) {
    return LSP_PREFIX + language;
  }

  public static final String OPS = BASE + "/ops";
  public static final String OPS_DEAD_LETTER_SUBMISSIONS = OPS + "/dead-letter-submissions";
  public static final String OPS_DEAD_LETTER_REPLAY =
      OPS_DEAD_LETTER_SUBMISSIONS + "/replay";
  public static final String OPS_RUNNERS_STATUS = OPS + "/runners/status";
  public static final String OPS_RUNNERS_JOBS = OPS + "/runners/jobs";
  public static final String OPS_RUNNERS_WARM_MAVEN = OPS + "/runners/warm/maven";
  public static final String OPS_RUNNERS_WARM_LSP = OPS + "/runners/warm/lsp";
  public static final String OPS_RUNNERS_WARM_POOL = OPS + "/runners/warm/pool";
  public static final String OPS_RUNNERS_WARM_INFRA = OPS + "/runners/warm/infra";

  public static final String SUBMISSION_EVENTS_PATTERN = ".*/api/v1/submissions/[^/]+/events";

  private ApiPaths() {}
}
