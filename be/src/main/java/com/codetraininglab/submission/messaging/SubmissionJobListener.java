package com.codetraininglab.submission.messaging;

import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.submission.application.SubmissionProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubmissionJobListener {

  private final SubmissionProcessor processor;

  public SubmissionJobListener(SubmissionProcessor processor) {
    this.processor = processor;
  }

  @RabbitListener(queues = RabbitMqConfig.SUBMISSION_QUEUE, concurrency = "2-4")
  public void onMessage(SubmissionJobMessage message) {
    processor.process(message.submissionId());
  }
}
