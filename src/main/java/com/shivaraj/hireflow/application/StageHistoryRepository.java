package com.shivaraj.hireflow.application;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StageHistoryRepository extends JpaRepository<StageHistory, Long> {

	List<StageHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
