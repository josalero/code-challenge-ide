package com.codetraininglab.platform.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class CtlMailServiceTest {

  @Mock private JavaMailSender javaMailSender;
  @Mock private ObjectProvider<JavaMailSender> mailSenderProvider;
  @Mock private MimeMessage mimeMessage;

  @Test
  void sendHtmlEmailDispatchesMessage() throws Exception {
    CtlMailProperties props =
        new CtlMailProperties(
            true, "smtp.test", 587, "noreply@test.com", "secret", "Code Training Lab", "http://localhost/login", false);
    when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    CtlMailService mailService = new CtlMailService(props, mailSenderProvider);

    mailService.sendHtmlEmail("user@test.com", "Subject", "<p>Hello</p>");

    verify(javaMailSender).send(mimeMessage);
  }

  @Test
  void sendHtmlEmailSkipsWhenNotConfigured() {
    CtlMailProperties disabled =
        new CtlMailProperties(false, "", 587, "", "", "Code Training Lab", "http://localhost/login", false);
    CtlMailService disabledService = new CtlMailService(disabled, mailSenderProvider);

    disabledService.sendHtmlEmail("user@test.com", "Subject", "<p>Hello</p>");
  }

  @Test
  void sendHtmlEmailWrapsFailures() throws Exception {
    CtlMailProperties props =
        new CtlMailProperties(
            true, "smtp.test", 587, "noreply@test.com", "secret", "Code Training Lab", "http://localhost/login", false);
    when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
    when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));
    CtlMailService mailService = new CtlMailService(props, mailSenderProvider);

    assertThatThrownBy(() -> mailService.sendHtmlEmail("user@test.com", "Subject", "<p>Hi</p>"))
        .isInstanceOf(CtlMailSendException.class);
  }
}
