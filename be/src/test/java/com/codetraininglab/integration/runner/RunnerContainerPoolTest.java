package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RunnerContainerPoolTest {

  @Test
  void isEnabledReflectsProperty() {
    var enabled = pool(CtlPropertiesTestFixtures.defaults(), Clock.systemUTC());
    assertThat(enabled.isEnabled()).isTrue();

    var disabled = pool(poolDisabledProperties(), Clock.systemUTC());
    assertThat(disabled.isEnabled()).isFalse();
  }

  @Test
  void adoptAndEvictNoOpWhenPoolDisabled() {
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    RunnerContainerPool pool = pool(poolDisabledProperties(), clock);

    pool.adoptExistingContainers();
    pool.evictIdleContainers();
  }

  @Test
  void executeReturnsRunnerFailureWhenContainerCannotStart() {
    RunnerContainerPool pool = pool(CtlPropertiesTestFixtures.defaults(), Clock.systemUTC());
    RunnerResult result =
        pool.execute(
            "ctl-nonexistent-runner-image-for-unit-test:latest",
            Path.of("challenges/reverse-string"),
            "maven",
            "{\"submission_id\":\"warm\"}",
            RunnerJobPayload.RunnerLimits.defaults());

    assertThat(result.status()).isEqualTo(RunnerStatus.FAILED.name());
    assertThat(result.tests()).hasSize(1);
    assertThat(result.tests().getFirst().name()).isEqualTo("runner");
    assertThat(result.tests().getFirst().message()).isNotBlank();
  }

  @Test
  void evictIdleContainersRemovesStaleEntry() throws Exception {
    Instant now = Instant.parse("2026-06-01T12:00:00Z");
    Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    RunnerContainerPool pool = pool(CtlPropertiesTestFixtures.defaults(), clock);

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<String, Object> pools =
        (ConcurrentHashMap<String, Object>) poolsField(pool).get(pool);
    Object pooled =
        newPooledRunner(
            "stale-image:local",
            "ctl-runner-pool-stale-image-local",
            null,
            now.minusSeconds(3600));
    pools.put("stale-image:local", pooled);

    pool.evictIdleContainers();

    assertThat(pools).isEmpty();
  }

  @Test
  void runnerPoolExceptionSupportsMessageAndCause() {
    var ex = new RunnerContainerPool.RunnerPoolException("boom", new IllegalStateException("root"));
    assertThat(ex).hasMessage("boom").hasCauseInstanceOf(IllegalStateException.class);
  }

  private static RunnerContainerPool pool(CtlProperties properties, Clock clock) {
    return new RunnerContainerPool(properties, JsonMapper.builder().build(), clock);
  }

  private static CtlProperties poolDisabledProperties() {
    var base = CtlPropertiesTestFixtures.defaults();
    return new CtlProperties(
        base.registrationEnabled(),
        base.jwtSecret(),
        base.jwtExpirationHours(),
        base.corsAllowedOrigins(),
        base.challengesPath(),
        base.runnerJava26Image(),
        base.runnerMavenCacheVolume(),
        false,
        base.runnerPoolIdleMinutes(),
        base.lspImages(),
        base.lspIdleMinutes(),
        base.idempotencyTtlHours(),
        base.aiProvider(),
        base.openrouterApiKey(),
        base.openrouterModel(),
        base.ollamaBaseUrl(),
        base.ollamaModel(),
        base.dockerEnabled(),
        base.lspEnabled(),
        base.runnerPoolWarmOnStartup(), base.userMaxStartedChallenges());
  }

  private static Field poolsField(RunnerContainerPool pool) throws NoSuchFieldException {
    Field field = RunnerContainerPool.class.getDeclaredField("pools");
    field.setAccessible(true);
    return field;
  }

  private static Object newPooledRunner(
      String image, String containerName, String containerId, Instant lastUsed)
      throws Exception {
    Class<?> pooledClass =
        Class.forName("com.codetraininglab.integration.runner.RunnerContainerPool$PooledRunner");
    var ctor =
        pooledClass.getDeclaredConstructor(
            String.class,
            String.class,
            ReentrantLock.class,
            AtomicReference.class,
            AtomicReference.class,
            AtomicReference.class);
    ctor.setAccessible(true);
    return ctor.newInstance(
        image,
        containerName,
        new ReentrantLock(),
        new AtomicReference<>(containerId),
        new AtomicReference<>(lastUsed),
        new AtomicReference<String>(null));
  }
}
