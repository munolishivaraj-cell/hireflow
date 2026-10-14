package com.shivaraj.hireflow.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplyRequest(@NotNull Long jobId, @NotNull Long candidateId, @Size(max = 2000) String coverNote) {
}
