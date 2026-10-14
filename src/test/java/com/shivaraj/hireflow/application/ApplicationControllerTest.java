package com.shivaraj.hireflow.application;

import com.shivaraj.hireflow.common.ApiException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ApplicationService applicationService;

	@Test
	void missingJobIdReturnsValidationProblem() throws Exception {
		mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
				{"candidateId": 1}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.jobId").exists());
	}

	@Test
	void rateLimitedApplyReturns429() throws Exception {
		given(applicationService.apply(any()))
			.willThrow(new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Candidate 1 may submit at most 5"));

		mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
				{"jobId": 1, "candidateId": 1}
				"""))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.detail").value("Candidate 1 may submit at most 5"));
	}

	@Test
	void unknownStageValueIsBadRequest() throws Exception {
		mvc.perform(patch("/api/applications/5/stage").contentType(MediaType.APPLICATION_JSON).content("""
				{"stage": "PROMOTED"}
				"""))
			.andExpect(status().isBadRequest());
	}
}
