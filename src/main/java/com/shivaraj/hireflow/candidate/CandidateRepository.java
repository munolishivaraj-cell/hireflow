package com.shivaraj.hireflow.candidate;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

	boolean existsByEmailIgnoreCase(String email);

	@EntityGraph(attributePaths = "skills")
	Optional<Candidate> findWithSkillsById(Long id);
}
