package com.codetraininglab.platform.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CtlMailService {

  private static final Logger log = LoggerFactory.getLogger(CtlMailService.class);

  private final CtlMailProperties mailProperties;
  private final ObjectProvider<JavaMailSender> mailSender;

  public CtlMailService(CtlMailProperties mailProperties, ObjectProvider<JavaMailSender> mailSender) {
    this.mailProperties = mailProperties;
    this.mailSender = mailSender;
  }

  public boolean isReady() {
    return mailProperties.isReady() && mailSender.getIfAvailable() != null;
  }

  public void sendHtmlEmail(String to, String subject, String htmlBody) {
    if (!isReady()) {
      log.warn("Mail is not configured; skipped sending message to {}", maskEmail(to));
      return;
    }
    try {
      JavaMailSender sender = mailSender.getIfAvailable();
      if (sender == null) {
        log.warn("Mail sender unavailable; skipped sending message to {}", maskEmail(to));
        return;
      }
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
      helper.setFrom(new InternetAddress(mailProperties.username(), mailProperties.fromName()));
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);
      sender.send(message);
      log.info("Sent email to {}", maskEmail(to));
    } catch (Exception ex) {
      log.error("Failed to send email to {}", maskEmail(to), ex);
      throw new CtlMailSendException("Could not send email", ex);
    }
  }

  private static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }
    int at = email.indexOf('@');
    if (at <= 1) {
      return "***" + email.substring(at);
    }
    return email.charAt(0) + "***" + email.substring(at);
  }
}
