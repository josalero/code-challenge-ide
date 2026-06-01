package com.codetraininglab.submission.application;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.nio.file.Path;

/** Loaded in a short transaction before the long-running Docker sandbox call. */
record SubmissionProcessingContext(
    SubmissionEntity submission,
    ChallengeEntity challenge,
    LanguageRuntimeEntity runtime,
    Path challengeDir) {}
