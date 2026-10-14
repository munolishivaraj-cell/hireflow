package com.shivaraj.hireflow.common;

import java.net.URI;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sends visitors of the bare demo URL straight to the interactive API docs. */
@RestController
@Hidden
public class HomeController {

	@GetMapping("/")
	ResponseEntity<Void> home() {
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/swagger-ui.html")).build();
	}
}
