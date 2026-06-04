package com.codetraininglab.platform.security;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = extractToken(request);
    if (token != null) {
      try {
        UUID userId = jwtService.parseUserId(token);
        UserRole role = jwtService.parseRole(token);
        boolean active =
            userRepository
                .findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .isPresent();
        if (active) {
          var auth =
              new UsernamePasswordAuthenticationToken(
                  userId, null, List.of(new SimpleGrantedAuthority(role.authority())));
          SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
          SecurityContextHolder.clearContext();
        }
      } catch (RuntimeException ignored) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private static String extractToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    if (isTokenQueryAuth(request)) {
      String query = request.getParameter("access_token");
      if (query != null && !query.isBlank()) {
        return query;
      }
    }
    return null;
  }

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return false;
  }

  private static boolean isTokenQueryAuth(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri == null) {
      return false;
    }
    if ("GET".equalsIgnoreCase(request.getMethod())
        && uri.matches(ApiPaths.SUBMISSION_EVENTS_PATTERN)) {
      return true;
    }
    return uri.startsWith(ApiPaths.LSP_PREFIX);
  }
}
