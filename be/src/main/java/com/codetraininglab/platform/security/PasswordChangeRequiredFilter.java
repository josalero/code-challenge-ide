package com.codetraininglab.platform.security;

import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Blocks API access for users who must change a temporary password, except profile and password
 * change endpoints.
 */
@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

  private static final Set<String> ALLOWED_METHOD_PATHS =
      Set.of(
          HttpMethod.GET.name() + " " + ApiPaths.ME,
          HttpMethod.POST.name() + " " + ApiPaths.ME_PASSWORD);

  private final UserRepository userRepository;

  public PasswordChangeRequiredFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof UUID userId
        && requiresPasswordChange(userId)
        && !isAllowedDuringPasswordChange(request)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response
          .getWriter()
          .write("{\"message\":\"Password change required before using the application\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean requiresPasswordChange(UUID userId) {
    return userRepository
        .findById(userId)
        .filter(user -> user.getDeletedAt() == null)
        .map(user -> user.isPasswordMustChange())
        .orElse(false);
  }

  private static boolean isAllowedDuringPasswordChange(HttpServletRequest request) {
    String key = request.getMethod() + " " + request.getRequestURI();
    return ALLOWED_METHOD_PATHS.contains(key);
  }
}
