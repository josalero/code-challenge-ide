package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.operations.api.DeadLetterReplayResponse;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueServiceTest {

  @Mock private RabbitTemplate rabbitTemplate;

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void peekReturnsEmptyWhenQueueIsEmpty() {
    when(rabbitTemplate.receive(eq(RabbitMqConfig.SUBMISSION_DLQ), anyLong())).thenReturn(null);
    DeadLetterQueueService svc = new DeadLetterQueueService(rabbitTemplate, jsonMapper);
    assertThat(svc.peek(5)).isEmpty();
  }

  @Test
  void peekParsesJobAndRequeuesMessage() throws Exception {
    UUID submissionId = UUID.randomUUID();
    byte[] body =
        jsonMapper.writeValueAsBytes(new SubmissionJobMessage(submissionId));
    MessageProperties props = new MessageProperties();
    Message message = new Message(body, props);
    when(rabbitTemplate.receive(eq(RabbitMqConfig.SUBMISSION_DLQ), anyLong())).thenReturn(message);

    DeadLetterQueueService svc = new DeadLetterQueueService(rabbitTemplate, jsonMapper);
    var results = svc.peek(1);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().submissionId()).isEqualTo(submissionId);
    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(rabbitTemplate).send(eq(RabbitMqConfig.SUBMISSION_DLQ), captor.capture());
    assertThat(new String(captor.getValue().getBody(), StandardCharsets.UTF_8))
        .contains(submissionId.toString());
  }

  @Test
  void replayMovesMessagesToSubmissionQueue() throws Exception {
    UUID submissionId = UUID.randomUUID();
    byte[] body =
        jsonMapper.writeValueAsBytes(new SubmissionJobMessage(submissionId));
    Message message = new Message(body, new MessageProperties());
    when(rabbitTemplate.receive(eq(RabbitMqConfig.SUBMISSION_DLQ), anyLong()))
        .thenReturn(message)
        .thenReturn(null);

    DeadLetterQueueService svc = new DeadLetterQueueService(rabbitTemplate, jsonMapper);
    DeadLetterReplayResponse response = svc.replay(5);

    assertThat(response.replayed()).isEqualTo(1);
    assertThat(response.items().getFirst().submissionId()).isEqualTo(submissionId);
    verify(rabbitTemplate).send(eq(RabbitMqConfig.SUBMISSION_QUEUE), eq(message));
  }

  @Test
  void peekReturnsUnparseableMessageMetadata() {
    Message message = new Message("not-json".getBytes(StandardCharsets.UTF_8), new MessageProperties());
    when(rabbitTemplate.receive(eq(RabbitMqConfig.SUBMISSION_DLQ), anyLong())).thenReturn(message);

    DeadLetterQueueService svc = new DeadLetterQueueService(rabbitTemplate, jsonMapper);
    var results = svc.peek(1);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().submissionId()).isNull();
    assertThat(results.getFirst().errorHint()).isEqualTo("Unparseable message");
  }
}
