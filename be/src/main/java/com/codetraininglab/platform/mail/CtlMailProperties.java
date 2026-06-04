package com.codetraininglab.platform.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ctl.mail")
public record CtlMailProperties(
    boolean enabled,
    String host,
    int port,
    String username,
    String password,
    String fromName,
    String loginUrl,
    boolean debug) {

  public CtlMailProperties {
    host = host == null ? "" : host.trim();
    username = username == null ? "" : username.trim();
    password = password == null ? "" : password.trim();
    fromName = fromName == null || fromName.isBlank() ? "Code Training Lab" : fromName.trim();
    loginUrl = loginUrl == null || loginUrl.isBlank() ? "http://localhost:5173/login" : loginUrl.trim();
  }

  public boolean isReady() {
    return enabled && !host.isBlank() && !username.isBlank() && !password.isBlank();
  }
}
