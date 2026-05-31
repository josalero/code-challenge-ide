package com.codetraininglab.submission.application;

import static org.mockito.Mockito.verify;

import com.codetraininglab.submission.messaging.SubmissionJobListener;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubmissionJobListenerTest {

  @Mock private SubmissionProcessor processor;

  @InjectMocks private SubmissionJobListener listener;

  @Test
  void delegatesToProcessor() {
    UUID id = UUID.randomUUID();
    listener.onMessage(new SubmissionJobMessage(id));
    verify(processor).process(id);
  }
}
