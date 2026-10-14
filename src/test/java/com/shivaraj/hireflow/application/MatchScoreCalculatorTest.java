package com.shivaraj.hireflow.application;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchScoreCalculatorTest {

	private final MatchScoreCalculator calculator = new MatchScoreCalculator();

	@Test
	void perfectMatchScoresHundred() {
		assertThat(calculator.score(Set.of("java", "spring boot"), 3, Set.of("java", "spring boot", "react"), 5))
			.isEqualTo(100);
	}

	@Test
	void halfTheSkillsAndHalfTheExperience() {
		// 0.5 * 70 + 0.5 * 30
		assertThat(calculator.score(Set.of("java", "kafka"), 4, Set.of("java"), 2)).isEqualTo(50);
	}

	@Test
	void noSkillsMatchedButEnoughExperience() {
		assertThat(calculator.score(Set.of("go", "rust"), 2, Set.of("java"), 10)).isEqualTo(30);
	}

	@Test
	void jobWithNoExperienceRequirementGivesFullExperiencePoints() {
		assertThat(calculator.score(Set.of("java"), 0, Set.of("java"), 0)).isEqualTo(100);
	}
}
