package com.codetraininglab.platform.security;

import com.codetraininglab.platform.config.CtlProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationHours;
  private final Clock clock;

  public JwtService(CtlProperties properties, Clock clock) {
    if (properties.jwtSecret() == null || properties.jwtSecret().length() < 32) {
      throw new IllegalStateException("ctl.jwt-secret must be at least 32 characters");
    }
    this.key = Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationHours = properties.jwtExpirationHours();
    this.clock = clock;
  }

  public String createToken(UUID userId, String email) {
    Instant now = clock.instant();
    Instant expiry = now.plusSeconds(expirationHours * 3600L);
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(key)
        .compact();
  }

  public UUID parseUserId(String token) {
    Claims claims = parseClaims(token);
    return UUID.fromString(claims.getSubject());
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .clock(() -> Date.from(clock.instant()))
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
