package com.codetraininglab.platform.mail;

import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(prefix = "ctl.mail", name = "enabled", havingValue = "true")
public class CtlMailConfiguration {

  @Bean
  JavaMailSender javaMailSender(CtlMailProperties props) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(props.host());
    mailSender.setPort(props.port());
    mailSender.setUsername(props.username());
    mailSender.setPassword(props.password());

    Properties javaMailProps = mailSender.getJavaMailProperties();
    javaMailProps.put("mail.transport.protocol", "smtp");
    javaMailProps.put("mail.smtp.auth", "true");
    javaMailProps.put("mail.smtp.starttls.enable", "true");
    javaMailProps.put("mail.smtp.ssl.trust", props.host());
    if (props.debug()) {
      javaMailProps.put("mail.debug", "true");
    }
    return mailSender;
  }
}
