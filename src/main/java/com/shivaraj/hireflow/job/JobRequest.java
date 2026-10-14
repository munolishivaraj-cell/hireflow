package com.shivaraj.hireflow.job;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record JobRequest(
		@NotBlank @Size(max = 150) String title,
		@NotBlank @Size(max = 100) String department,
		@NotBlank @Size(max = 100) String location,
		@Min(0) @Max(40) int minExperienceYears,
		@Size(max = 5000) String description,
		@NotEmpty Set<@NotBlank String> skills) {
}
