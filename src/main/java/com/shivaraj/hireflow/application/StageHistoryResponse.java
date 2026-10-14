package com.shivaraj.hireflow.application;

import java.time.Instant;

public record StageHistoryResponse(Stage fromStage, Stage toStage, String note, Instant changedAt) {

	static StageHistoryResponse from(StageHistory h) {
		return new StageHistoryResponse(h.getFromStage(), h.getToStage(), h.getNote(), h.getChangedAt());
	}
}
