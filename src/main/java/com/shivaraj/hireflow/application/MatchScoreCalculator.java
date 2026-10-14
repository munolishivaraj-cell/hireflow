package com.shivaraj.hireflow.application;

import java.util.Set;

import com.shivaraj.hireflow.candidate.Candidate;
import com.shivaraj.hireflow.job.Job;

import org.springframework.stereotype.Component;

/**
 * Scores a candidate against a job from 0 to 100: 70 points for required-skill coverage and 30 points for
 * meeting the minimum experience.
 */
@Component
public class MatchScoreCalculator {

	static final int SKILL_WEIGHT = 70;

	static final int EXPERIENCE_WEIGHT = 30;

	public int score(Job job, Candidate candidate) {
		return score(job.getSkills(), job.getMinExperienceYears(), candidate.getSkills(),
				candidate.getExperienceYears());
	}

	int score(Set<String> requiredSkills, int minYears, Set<String> candidateSkills, int candidateYears) {
		double skillRatio = requiredSkills.isEmpty() ? 1.0
				: (double) requiredSkills.stream().filter(candidateSkills::contains).count() / requiredSkills.size();
		double experienceRatio = minYears == 0 ? 1.0 : Math.min(1.0, (double) candidateYears / minYears);
		return (int) Math.round(skillRatio * SKILL_WEIGHT + experienceRatio * EXPERIENCE_WEIGHT);
	}
}
