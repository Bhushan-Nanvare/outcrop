package dev.outcrop.analysis;

import dev.outcrop.clone.ClonedRepository;
import dev.outcrop.clone.CloneFailedException;
import dev.outcrop.clone.RepositoryCloner;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisService {

	private final AnalysisRepository repository;

	private final RepositoryCloner cloner;

	AnalysisService(AnalysisRepository repository, RepositoryCloner cloner) {
		this.repository = repository;
		this.cloner = cloner;
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

	public Analysis run(UUID id) {
		Analysis analysis = repository.findById(id).orElseThrow(() -> new AnalysisNotFoundException(id));
		analysis.start();
		repository.save(analysis);

		try (ClonedRepository cloned = cloner.cloneRepository(analysis.getRepositoryUrl())) {
			analysis.recordClone(cloned.commitSha(), cloned.javaFileCount());
			analysis.complete();
		}
		catch (CloneFailedException e) {
			analysis.fail(e.getMessage());
		}
		return repository.save(analysis);
	}

}
