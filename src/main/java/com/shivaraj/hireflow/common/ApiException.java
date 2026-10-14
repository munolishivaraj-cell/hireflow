package com.shivaraj.hireflow.common;

import org.springframework.http.HttpStatus;

/**
 * Business error that maps directly to an HTTP status and an RFC 9457 problem response.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public static ApiException notFound(String what, Object id) {
		return new ApiException(HttpStatus.NOT_FOUND, what + " " + id + " not found");
	}

	public static ApiException conflict(String message) {
		return new ApiException(HttpStatus.CONFLICT, message);
	}
}
