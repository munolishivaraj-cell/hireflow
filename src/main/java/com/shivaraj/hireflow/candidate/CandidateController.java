package com.shivaraj.hireflow.candidate;

import java.util.List;

import com.shivaraj.hireflow.application.ApplicationResponse;
import com.shivaraj.hireflow.application.ApplicationService;
import com.shivaraj.hireflow.notification.NotificationResponse;
import com.shivaraj.hireflow.notification.NotificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

	private final CandidateService candidateService;

	private final ApplicationService applicationService;

	private final NotificationService notificationService;

	public CandidateController(CandidateService candidateService, ApplicationService applicationService,
			NotificationService notificationService) {
		this.candidateService = candidateService;
		this.applicationService = applicationService;
		this.notificationService = notificationService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CandidateResponse register(@Valid @RequestBody CandidateRequest request) {
		return candidateService.register(request);
	}

	@GetMapping("/{id}")
	public CandidateResponse get(@PathVariable Long id) {
		return candidateService.get(id);
	}

	@GetMapping("/{id}/applications")
	public List<ApplicationResponse> applications(@PathVariable Long id) {
		return applicationService.forCandidate(id);
	}

	@GetMapping("/{id}/notifications")
	public List<NotificationResponse> notifications(@PathVariable Long id) {
		return notificationService.forCandidate(id);
	}
}
