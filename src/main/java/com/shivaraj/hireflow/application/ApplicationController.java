package com.shivaraj.hireflow.application;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

	private final ApplicationService applicationService;

	public ApplicationController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping("/api/applications")
	@ResponseStatus(HttpStatus.CREATED)
	public ApplicationResponse apply(@Valid @RequestBody ApplyRequest request) {
		return applicationService.apply(request);
	}

	@GetMapping("/api/applications/{id}")
	public ApplicationResponse get(@PathVariable Long id) {
		return applicationService.get(id);
	}

	@PatchMapping("/api/applications/{id}/stage")
	public ApplicationResponse changeStage(@PathVariable Long id, @Valid @RequestBody StageChangeRequest request) {
		return applicationService.changeStage(id, request);
	}

	@GetMapping("/api/applications/{id}/history")
	public List<StageHistoryResponse> history(@PathVariable Long id) {
		return applicationService.history(id);
	}

	@GetMapping("/api/jobs/{jobId}/applications")
	public List<ApplicationResponse> forJob(@PathVariable Long jobId, @RequestParam(required = false) Stage stage) {
		return applicationService.rankedForJob(jobId, stage);
	}

	@GetMapping("/api/jobs/{jobId}/pipeline")
	public PipelineSummary pipeline(@PathVariable Long jobId) {
		return applicationService.pipeline(jobId);
	}
}
