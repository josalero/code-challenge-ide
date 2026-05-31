package com.codetraininglab.operations.api;

import java.util.List;

public record DeadLetterReplayResponse(int replayed, List<DeadLetterSubmissionResponse> items) {}
