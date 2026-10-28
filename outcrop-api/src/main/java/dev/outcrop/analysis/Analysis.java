package dev.outcrop.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis")
public class Analysis {

	@Id
	private UUID id;

	@Column(name = "repository_url", nullable = false)
	private String repositoryUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@Column(name = "failure_reason")
	private String failureReason;

	protected Analysis() {
	}

	public Analysis(String repositoryUrl) {
		this.id = UUID.randomUUID();
		this.repositoryUrl = repositoryUrl;
		this.status = AnalysisStatus.PENDING;
		this.createdAt = Instant.now();
	}

	public void start() {
		this.status = AnalysisStatus.RUNNING;
		this.startedAt = Instant.now();
	}

	public void complete() {
		this.status = AnalysisStatus.COMPLETE;
		this.finishedAt = Instant.now();
	}

	public void fail(String reason) {
		this.status = AnalysisStatus.FAILED;
		this.finishedAt = Instant.now();
		this.failureReason = reason;
	}

	public UUID getId() {
		return id;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public AnalysisStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getFinishedAt() {
		return finishedAt;
	}

	public String getFailureReason() {
		return failureReason;
	}

}
