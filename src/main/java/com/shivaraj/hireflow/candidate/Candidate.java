package com.shivaraj.hireflow.candidate;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidates")
public class Candidate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "full_name")
	private String fullName;

	private String email;

	private String phone;

	@Column(name = "experience_years")
	private int experienceYears;

	@ElementCollection
	@CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
	@Column(name = "skill")
	private Set<String> skills = new LinkedHashSet<>();

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	protected Candidate() {
	}

	public Candidate(String fullName, String email, String phone, int experienceYears, Set<String> skills) {
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
		this.experienceYears = experienceYears;
		this.skills = new LinkedHashSet<>(skills);
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public int getExperienceYears() {
		return experienceYears;
	}

	public Set<String> getSkills() {
		return skills;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
