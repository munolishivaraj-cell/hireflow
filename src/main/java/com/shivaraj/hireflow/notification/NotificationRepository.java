package com.shivaraj.hireflow.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	boolean existsByEventId(String eventId);

	List<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
