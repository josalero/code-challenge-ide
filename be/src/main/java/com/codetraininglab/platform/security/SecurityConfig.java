package com.codetraininglab.platform.security;

import com.codetraininglab.platform.web.ApiPaths;
import com.codetraininglab.platform.config.CtlProperties;
import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  /** BCrypt password encoder for {@code users.password_hash} (Spring Security standard). */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtFilter,
      PasswordChangeRequiredFilter passwordChangeRequiredFilter,
      CtlProperties properties)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC)
                    .permitAll()
                    .requestMatchers(ApiPaths.HEALTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.AUTH_LOGIN)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.AUTH_REGISTER)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.AUTH_REGISTRATION_INFO)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.AUTH_PASSWORD_REQUIREMENTS)
                    .permitAll()
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.LANGUAGES)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.CHALLENGES, ApiPaths.CHALLENGES + "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(passwordChangeRequiredFilter, JwtAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(CtlProperties properties) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(
        Arrays.stream(properties.corsAllowedOrigins().split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList());
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
