package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.codetraininglab.platform.mail.CtlMailProperties;
import com.codetraininglab.platform.mail.CtlMailService;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserWelcomeEmailSenderTest {

  @Mock private CtlMailService mailService;

  private UserWelcomeEmailSender sender;

  @BeforeEach
  void setUp() throws Exception {
    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_34);
    freemarker.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
    CtlMailProperties props =
        new CtlMailProperties(
            true, "smtp.test", 587, "noreply@test", "secret", "Code Training Lab", "https://app.test/login", false);
    sender = new UserWelcomeEmailSender(mailService, props, freemarker);
  }

  @Test
  void renderWelcomeHtmlIncludesCredentialsAndLoginLink() throws Exception {
    String html =
        sender.renderWelcomeHtml("learner@test.com", "Ada Lovelace", "TempPass1", "USER");

    assertThat(html).contains("Ada Lovelace");
    assertThat(html).contains("learner@test.com");
    assertThat(html).contains("TempPass1");
    assertThat(html).contains("Learner");
    assertThat(html).contains("https://app.test/login");
  }

  @Test
  void sendWelcomeEmailUsesMailServiceWhenReady() {
    org.mockito.Mockito.when(mailService.isReady()).thenReturn(true);

    sender.sendWelcomeEmail("learner@test.com", "Ada Lovelace", "TempPass1", "USER");

    verify(mailService)
        .sendHtmlEmail(
            eq("learner@test.com"),
            eq("Your Code Training Lab account"),
            argThat(html -> html.contains("Ada Lovelace")));
  }
}
