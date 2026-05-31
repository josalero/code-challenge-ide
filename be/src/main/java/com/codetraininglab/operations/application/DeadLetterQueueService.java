package com.codetraininglab.operations.application;

import com.codetraininglab.operations.api.DeadLetterReplayResponse;
import com.codetraininglab.operations.api.DeadLetterSubmissionResponse;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class DeadLetterQueueService {

  private final RabbitTemplate rabbitTemplate;
  private final JsonMapper jsonMapper;

  public DeadLetterQueueService(RabbitTemplate rabbitTemplate, JsonMapper jsonMapper) {
    this.rabbitTemplate = rabbitTemplate;
    this.jsonMapper = jsonMapper;
  }

  public List<DeadLetterSubmissionResponse> peek(int limit) {
    return drain(limit, true);
  }

  public DeadLetterReplayResponse replay(int limit) {
    List<DeadLetterSubmissionResponse> items = drain(limit, false);
    return new DeadLetterReplayResponse(items.size(), items);
  }

  private List<DeadLetterSubmissionResponse> drain(int limit, boolean requeueToDlq) {
    int capped = Math.min(Math.max(limit, 1), 50);
    List<DeadLetterSubmissionResponse> results = new ArrayList<>();
    for (int i = 0; i < capped; i++) {
      Message message = rabbitTemplate.receive(RabbitMqConfig.SUBMISSION_DLQ, 200);
      if (message == null) {
        break;
      }
      results.add(toResponse(message));
      if (requeueToDlq) {
        rabbitTemplate.send(RabbitMqConfig.SUBMISSION_DLQ, message);
      } else {
        rabbitTemplate.send(RabbitMqConfig.SUBMISSION_QUEUE, message);
      }
    }
    return results;
  }

  private DeadLetterSubmissionResponse toResponse(Message message) {
    try {
      String body = new String(message.getBody(), StandardCharsets.UTF_8);
      SubmissionJobMessage job = jsonMapper.readValue(body, SubmissionJobMessage.class);
      return new DeadLetterSubmissionResponse(
          job.submissionId(),
          "DLQ",
          message.getMessageProperties().getTimestamp() != null
              ? message.getMessageProperties().getTimestamp().toInstant()
              : Instant.now(),
          headerAsString(message, "x-first-death-reason"));
    } catch (RuntimeException e) {
      return new DeadLetterSubmissionResponse(
          null, "DLQ", Instant.now(), "Unparseable message");
    }
  }

  private static String headerAsString(Message message, String key) {
    Object value = message.getMessageProperties().getHeader(key);
    return value != null ? value.toString() : null;
  }
}
