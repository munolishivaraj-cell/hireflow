package com.shivaraj.hireflow.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StageChangeRequest(@NotNull Stage stage, @Size(max = 500) String note) {
}
