package dev.outcrop.analysis;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisService {

	private final AnalysisRepository repository;

	AnalysisService(AnalysisRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public Analysis create(String repositoryUrl) {
		return repository.save(new Analysis(repositoryUrl));
	}

	@Transactional(readOnly = true)
	public Analysis get(UUID id) {
		return repository.findById(id).orElseThrow(() -> new AnalysisNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public List<Analysis> list() {
		return repository.findAllByOrderByCreatedAtDesc();
	}

}
