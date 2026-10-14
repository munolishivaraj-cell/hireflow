package com.shivaraj.hireflow.candidate;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CandidateRequest(
		@NotBlank @Size(max = 120) String fullName,
		@NotBlank @Email @Size(max = 180) String email,
		@Pattern(regexp = "^\\+?[0-9 ]{7,20}$", message = "must be a valid phone number") String phone,
		@Min(0) @Max(50) int experienceYears,
		@NotEmpty Set<@NotBlank String> skills) {
}
