package com.codetraininglab.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ctl")
public record CtlProperties(
    boolean registrationEnabled,
    String jwtSecret,
    int jwtExpirationHours,
    String corsAllowedOrigins,
    String challengesPath,
    String runnerJava26Image,
    String runnerMavenCacheVolume,
    String lspJavaImage,
    int lspIdleMinutes,
    int idempotencyTtlHours,
    String aiProvider,
    String openrouterApiKey,
    String openrouterModel,
    String ollamaBaseUrl,
    String ollamaModel,
    boolean dockerEnabled,
    boolean lspEnabled) {}
