package com.shivaraj.hireflow.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StageTest {

	@Test
	void happyPathMovesOneStepAtATime() {
		assertThat(Stage.APPLIED.canMoveTo(Stage.SCREENING)).isTrue();
		assertThat(Stage.SCREENING.canMoveTo(Stage.INTERVIEW)).isTrue();
		assertThat(Stage.INTERVIEW.canMoveTo(Stage.OFFER)).isTrue();
		assertThat(Stage.OFFER.canMoveTo(Stage.HIRED)).isTrue();
	}

	@Test
	void cannotSkipStages() {
		assertThat(Stage.APPLIED.canMoveTo(Stage.HIRED)).isFalse();
		assertThat(Stage.APPLIED.canMoveTo(Stage.OFFER)).isFalse();
		assertThat(Stage.SCREENING.canMoveTo(Stage.APPLIED)).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Stage.class, names = { "HIRED", "REJECTED", "WITHDRAWN" })
	void terminalStagesAllowNoFurtherMoves(Stage stage) {
		assertThat(stage.isTerminal()).isTrue();
		assertThat(stage.next()).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(value = Stage.class, names = { "APPLIED", "SCREENING", "INTERVIEW", "OFFER" })
	void activeStagesCanAlwaysBeRejectedOrWithdrawn(Stage stage) {
		assertThat(stage.canMoveTo(Stage.REJECTED)).isTrue();
		assertThat(stage.canMoveTo(Stage.WITHDRAWN)).isTrue();
	}
}
