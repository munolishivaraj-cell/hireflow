package com.shivaraj.hireflow.application;

import java.util.Set;

/**
 * Hiring pipeline stages. Each stage declares which stages it may move to, so invalid jumps such as
 * APPLIED to HIRED are rejected in one place.
 */
public enum Stage {

	APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED, WITHDRAWN;

	public Set<Stage> next() {
		return switch (this) {
			case APPLIED -> Set.of(SCREENING, REJECTED, WITHDRAWN);
			case SCREENING -> Set.of(INTERVIEW, REJECTED, WITHDRAWN);
			case INTERVIEW -> Set.of(OFFER, REJECTED, WITHDRAWN);
			case OFFER -> Set.of(HIRED, REJECTED, WITHDRAWN);
			case HIRED, REJECTED, WITHDRAWN -> Set.of();
		};
	}

	public boolean canMoveTo(Stage target) {
		return next().contains(target);
	}

	public boolean isTerminal() {
		return next().isEmpty();
	}
}
