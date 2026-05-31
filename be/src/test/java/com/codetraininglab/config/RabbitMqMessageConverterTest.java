package com.codetraininglab.platform.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import tools.jackson.databind.json.JsonMapper;

class RabbitMqMessageConverterTest {

  private final JacksonJsonMessageConverter converter =
      new JacksonJsonMessageConverter(JsonMapper.builder().build());

  @Test
  void roundTripsSubmissionJobMessage() {
    UUID submissionId = UUID.randomUUID();
    SubmissionJobMessage original = new SubmissionJobMessage(submissionId);

    Message message = converter.toMessage(original, null);
    Object restored = converter.fromMessage(message);

    assertThat(restored).isInstanceOf(SubmissionJobMessage.class);
    assertThat(((SubmissionJobMessage) restored).submissionId()).isEqualTo(submissionId);
  }
}
