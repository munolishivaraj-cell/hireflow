package com.shivaraj.hireflow.job;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "jobs")
public class Job {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private String department;

	private String location;

	@Column(name = "min_experience_years")
	private int minExperienceYears;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	private JobStatus status = JobStatus.OPEN;

	@ElementCollection
	@CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
	@Column(name = "skill")
	private Set<String> skills = new LinkedHashSet<>();

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Version
	private long version;

	protected Job() {
	}

	public Job(String title, String department, String location, int minExperienceYears, String description,
			Set<String> skills) {
		this.title = title;
		this.department = department;
		this.location = location;
		this.minExperienceYears = minExperienceYears;
		this.description = description;
		this.skills = new LinkedHashSet<>(skills);
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public boolean isOpen() {
		return status == JobStatus.OPEN;
	}

	public void close() {
		status = JobStatus.CLOSED;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDepartment() {
		return department;
	}

	public String getLocation() {
		return location;
	}

	public int getMinExperienceYears() {
		return minExperienceYears;
	}

	public String getDescription() {
		return description;
	}

	public JobStatus getStatus() {
		return status;
	}

	public Set<String> getSkills() {
		return skills;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
