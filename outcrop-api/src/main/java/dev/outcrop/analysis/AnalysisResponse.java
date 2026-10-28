package dev.outcrop.analysis;

import java.time.Instant;
import java.util.UUID;

public record AnalysisResponse(UUID id, String repositoryUrl, AnalysisStatus status, Instant createdAt,
		Instant startedAt, Instant finishedAt, String failureReason) {

	static AnalysisResponse from(Analysis analysis) {
		return new AnalysisResponse(analysis.getId(), analysis.getRepositoryUrl(), analysis.getStatus(),
				analysis.getCreatedAt(), analysis.getStartedAt(), analysis.getFinishedAt(),
				analysis.getFailureReason());
	}

}
