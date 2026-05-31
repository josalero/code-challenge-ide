package com.codetraininglab.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17").withDatabaseName("code_training_lab_test");

  @Container
  static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3-management-alpine");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.rabbitmq.host", RABBIT::getHost);
    registry.add("spring.rabbitmq.port", () -> String.valueOf(RABBIT.getAmqpPort()));
    registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
    registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    registry.add("ctl.challenges-path", IntegrationTestBase::resolveChallengesPath);
  }

  private static String resolveChallengesPath() {
    Path cwd = Path.of(System.getProperty("user.dir"));
    Path direct = cwd.resolve("challenges");
    if (Files.isDirectory(direct)) {
      return direct.toString();
    }
    Path parent = cwd.getParent().resolve("challenges");
    if (Files.isDirectory(parent)) {
      return parent.toString();
    }
    return direct.toString();
  }
}
