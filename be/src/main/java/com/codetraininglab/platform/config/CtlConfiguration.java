package com.codetraininglab.platform.config;

import com.codetraininglab.platform.mail.CtlMailProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CtlProperties.class, CtlMailProperties.class})
public class CtlConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
