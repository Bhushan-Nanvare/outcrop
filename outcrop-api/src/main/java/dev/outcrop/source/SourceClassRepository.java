package dev.outcrop.source;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceClassRepository extends JpaRepository<SourceClass, UUID> {

	List<SourceClass> findAllByAnalysisId(UUID analysisId);

	long countByAnalysisId(UUID analysisId);

}
