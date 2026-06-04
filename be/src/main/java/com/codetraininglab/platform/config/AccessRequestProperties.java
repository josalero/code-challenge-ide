package com.codetraininglab.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ctl.access")
public record AccessRequestProperties(boolean requestsEnabled, String notifyEmail) {

  public AccessRequestProperties {
    notifyEmail = notifyEmail == null ? "" : notifyEmail.trim();
  }

  public boolean isAcceptingRequests() {
    return requestsEnabled && !notifyEmail.isBlank();
  }
}
