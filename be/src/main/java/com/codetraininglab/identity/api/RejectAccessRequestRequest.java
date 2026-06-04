package com.codetraininglab.identity.api;

import com.codetraininglab.domain.UserRole;
import jakarta.validation.constraints.Size;

public record RejectAccessRequestRequest(@Size(max = 2000) String reviewNotes) {}
