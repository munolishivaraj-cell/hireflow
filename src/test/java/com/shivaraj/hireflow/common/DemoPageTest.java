package com.shivaraj.hireflow.common;

import com.shivaraj.hireflow.application.ApplicationController;
import com.shivaraj.hireflow.application.ApplicationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class DemoPageTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ApplicationService applicationService;

	@Test
	void rootServesTheDemoDashboard() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
	}

	@Test
	void dashboardIsAStaticPage() throws Exception {
		mvc.perform(get("/index.html"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Apply as a demo candidate")))
			.andExpect(content().string(containsString("/api/jobs?size=50")));
	}
}
