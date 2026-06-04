package com.codetraininglab.identity.application;

import com.codetraininglab.platform.mail.CtlMailProperties;
import com.codetraininglab.platform.mail.CtlMailSendException;
import com.codetraininglab.platform.mail.CtlMailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserWelcomeEmailSender {

  private static final Logger log = LoggerFactory.getLogger(UserWelcomeEmailSender.class);
  private static final String TEMPLATE_NAME = "user_welcome.ftl";
  private static final String SUBJECT = "Your Code Training Lab account was created";

  private final CtlMailService mailService;
  private final CtlMailProperties mailProperties;
  private final Configuration freemarkerConfiguration;

  public UserWelcomeEmailSender(
      CtlMailService mailService,
      CtlMailProperties mailProperties,
      Configuration freemarkerConfiguration) {
    this.mailService = mailService;
    this.mailProperties = mailProperties;
    this.freemarkerConfiguration = freemarkerConfiguration;
  }

  /**
   * Sends a welcome email with the temporary password when mail is configured.
   *
   * @return {@code true} when the email was sent, {@code false} when mail is disabled
   * @throws ResponseStatusException when mail is configured but delivery fails
   */
  public boolean sendWelcomeEmail(
      String email, String fullName, String temporaryPassword, String role) {
    if (!mailService.isReady()) {
      log.info("Welcome email skipped for {} (mail disabled or not configured)", email);
      return false;
    }
    try {
      String html = renderWelcomeHtml(email, fullName, temporaryPassword, role);
      mailService.sendHtmlEmail(email, SUBJECT, html);
      return true;
    } catch (CtlMailSendException ex) {
      log.error("Welcome email could not be sent for {}", email, ex);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Welcome email could not be sent. Check mail configuration and try again.",
          ex);
    } catch (IOException | TemplateException ex) {
      log.error("Welcome email template failed for {}", email, ex);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Welcome email could not be prepared.", ex);
    }
  }

  String renderWelcomeHtml(
      String email, String fullName, String temporaryPassword, String role)
      throws IOException, TemplateException {
    Template template = freemarkerConfiguration.getTemplate(TEMPLATE_NAME);
    Map<String, Object> model = new HashMap<>();
    model.put("fullName", fullName);
    model.put("email", email);
    model.put("temporaryPassword", temporaryPassword);
    model.put("roleLabel", "ADMIN".equals(role) ? "Administrator" : "Learner");
    model.put("loginUrl", mailProperties.loginUrl());
    model.put("appName", mailProperties.fromName());

    StringWriter writer = new StringWriter();
    template.process(model, writer);
    return writer.toString();
  }
}
