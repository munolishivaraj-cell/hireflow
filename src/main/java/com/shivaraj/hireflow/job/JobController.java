package com.shivaraj.hireflow.job;

import com.shivaraj.hireflow.common.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobController {

	private final JobService jobService;

	public JobController(JobService jobService) {
		this.jobService = jobService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public JobResponse create(@Valid @RequestBody JobRequest request) {
		return jobService.create(request);
	}

	@GetMapping
	public PageResponse<JobResponse> searchOpenJobs(@RequestParam(required = false) String skill,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return jobService.searchOpen(skill, page, size);
	}

	@GetMapping("/{id}")
	public JobResponse get(@PathVariable Long id) {
		return jobService.get(id);
	}

	@PostMapping("/{id}/close")
	public JobResponse close(@PathVariable Long id) {
		return jobService.close(id);
	}
}
