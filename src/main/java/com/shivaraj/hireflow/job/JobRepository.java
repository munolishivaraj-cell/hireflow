package com.shivaraj.hireflow.job;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

	@EntityGraph(attributePaths = "skills")
	Optional<Job> findWithSkillsById(Long id);

	@Query("""
			select j from Job j
			where j.status = :status
			  and (:skill is null or :skill member of j.skills)
			""")
	Page<Job> search(@Param("status") JobStatus status, @Param("skill") String skill, Pageable pageable);
}
