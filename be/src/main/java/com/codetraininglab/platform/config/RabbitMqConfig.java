package com.codetraininglab.platform.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  public static final String SUBMISSION_QUEUE = "ctl.submissions";
  public static final String SUBMISSION_DLQ = "ctl.submissions.dlq";
  public static final String SUBMISSION_DLX = "ctl.submissions.dlx";

  @Bean
  DirectExchange submissionDeadLetterExchange() {
    return new DirectExchange(SUBMISSION_DLX);
  }

  @Bean
  Queue submissionQueue() {
    return QueueBuilder.durable(SUBMISSION_QUEUE)
        .deadLetterExchange(SUBMISSION_DLX)
        .deadLetterRoutingKey(SUBMISSION_DLQ)
        .build();
  }

  @Bean
  Queue submissionDeadLetterQueue() {
    return QueueBuilder.durable(SUBMISSION_DLQ).build();
  }

  @Bean
  Binding submissionDeadLetterBinding() {
    return BindingBuilder.bind(submissionDeadLetterQueue())
        .to(submissionDeadLetterExchange())
        .with(SUBMISSION_DLQ);
  }

  @Bean
  MessageConverter rabbitMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }
}
