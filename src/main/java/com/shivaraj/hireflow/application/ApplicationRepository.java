package com.shivaraj.hireflow.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

	boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

	/** Loads job and candidate in the same query so listing endpoints avoid N+1 selects. */
	@EntityGraph(attributePaths = { "job", "candidate" })
	Optional<JobApplication> findDetailedById(Long id);

	@EntityGraph(attributePaths = { "job", "candidate" })
	@Query("""
			select a from JobApplication a
			where a.job.id = :jobId and (:stage is null or a.stage = :stage)
			order by a.matchScore desc nulls last, a.createdAt asc
			""")
	List<JobApplication> findRankedForJob(@Param("jobId") Long jobId, @Param("stage") Stage stage);

	@EntityGraph(attributePaths = { "job", "candidate" })
	List<JobApplication> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

	/** Interface projection: only the aggregate columns are selected, no entities are hydrated. */
	interface StageCount {

		Stage getStage();

		long getTotal();
	}

	@Query("select a.stage as stage, count(a) as total from JobApplication a where a.job.id = :jobId group by a.stage")
	List<StageCount> countByStage(@Param("jobId") Long jobId);
}
