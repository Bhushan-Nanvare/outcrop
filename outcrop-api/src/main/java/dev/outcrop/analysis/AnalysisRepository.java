package dev.outcrop.analysis;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

	List<Analysis> findAllByOrderByCreatedAtDesc();

}
