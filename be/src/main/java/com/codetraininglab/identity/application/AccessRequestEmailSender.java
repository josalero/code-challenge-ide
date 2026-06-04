package com.codetraininglab.identity.application;

import com.codetraininglab.platform.config.AccessRequestProperties;
import com.codetraininglab.platform.mail.CtlMailProperties;
import com.codetraininglab.platform.mail.CtlMailSendException;
import com.codetraininglab.platform.mail.CtlMailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccessRequestEmailSender {

  private static final Logger log = LoggerFactory.getLogger(AccessRequestEmailSender.class);
  private static final String ADMIN_TEMPLATE = "access_request_admin.ftl";
  private static final String CONFIRMATION_TEMPLATE = "access_request_confirmation.ftl";
  private static final DateTimeFormatter SUBMITTED_AT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

  private final CtlMailService mailService;
  private final CtlMailProperties mailProperties;
  private final AccessRequestProperties accessRequestProperties;
  private final Configuration freemarkerConfiguration;
  private final Clock clock;

  public AccessRequestEmailSender(
      CtlMailService mailService,
      CtlMailProperties mailProperties,
      AccessRequestProperties accessRequestProperties,
      Configuration freemarkerConfiguration,
      Clock clock) {
    this.mailService = mailService;
    this.mailProperties = mailProperties;
    this.accessRequestProperties = accessRequestProperties;
    this.freemarkerConfiguration = freemarkerConfiguration;
    this.clock = clock;
  }

  public void sendAccessRequestEmails(String email, String fullName, String message) {
    if (!mailService.isReady()) {
      throw new IllegalStateException("Mail is not configured");
    }
    String submittedAt = SUBMITTED_AT.format(clock.instant());
    String adminUsersUrl = adminUsersUrl();
    String note = message == null || message.isBlank() ? "(No message provided)" : message.trim();

    sendAdminNotification(email, fullName, note, submittedAt, adminUsersUrl);
    sendRequesterConfirmation(email, fullName);
  }

  private void sendAdminNotification(
      String email, String fullName, String message, String submittedAt, String adminUsersUrl) {
    try {
      Template template = freemarkerConfiguration.getTemplate(ADMIN_TEMPLATE);
      Map<String, Object> model = new HashMap<>();
      model.put("appName", mailProperties.fromName());
      model.put("fullName", fullName);
      model.put("email", email);
      model.put("message", message);
      model.put("submittedAt", submittedAt);
      model.put("adminUsersUrl", adminUsersUrl);

      String html = render(template, model);
      mailService.sendHtmlEmail(
          accessRequestProperties.notifyEmail(),
          "Access request: " + fullName,
          html);
    } catch (IOException | TemplateException ex) {
      log.error("Access request admin email template failed for {}", email, ex);
      throw new CtlMailSendException("Could not send access request notification", ex);
    }
  }

  private void sendRequesterConfirmation(String email, String fullName) {
    try {
      Template template = freemarkerConfiguration.getTemplate(CONFIRMATION_TEMPLATE);
      Map<String, Object> model = new HashMap<>();
      model.put("appName", mailProperties.fromName());
      model.put("fullName", fullName);
      model.put("loginUrl", mailProperties.loginUrl());

      String html = render(template, model);
      mailService.sendHtmlEmail(
          email, "We received your " + mailProperties.fromName() + " access request", html);
    } catch (IOException | TemplateException ex) {
      log.warn("Access request confirmation email failed for {}", email, ex);
    } catch (CtlMailSendException ex) {
      log.warn("Access request confirmation email could not be sent for {}", email, ex);
    }
  }

  private String adminUsersUrl() {
    String loginUrl = mailProperties.loginUrl();
    if (loginUrl.endsWith("/login")) {
      return loginUrl.substring(0, loginUrl.length() - "/login".length()) + "/admin/users";
    }
    if (loginUrl.endsWith("/login/")) {
      return loginUrl.substring(0, loginUrl.length() - "/login/".length()) + "/admin/users";
    }
    return loginUrl.replaceAll("/+$", "") + "/admin/users";
  }

  private static String render(Template template, Map<String, Object> model)
      throws IOException, TemplateException {
    StringWriter writer = new StringWriter();
    template.process(model, writer);
    return writer.toString();
  }
}
