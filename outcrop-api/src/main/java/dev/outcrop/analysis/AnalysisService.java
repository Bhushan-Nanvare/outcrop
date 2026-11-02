package dev.outcrop.analysis;

import dev.outcrop.clone.CloneFailedException;
import dev.outcrop.clone.ClonedRepository;
import dev.outcrop.clone.RepositoryCloner;
import dev.outcrop.parse.JavaSourceParser;
import dev.outcrop.parse.ParsedSources;
import dev.outcrop.source.SourceWriter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisService {

	private final AnalysisRepository repository;

	private final RepositoryCloner cloner;

	private final JavaSourceParser parser;

	private final SourceWriter sourceWriter;

	AnalysisService(AnalysisRepository repository, RepositoryCloner cloner, JavaSourceParser parser,
			SourceWriter sourceWriter) {
		this.repository = repository;
		this.cloner = cloner;
		this.parser = parser;
		this.sourceWriter = sourceWriter;
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

			ParsedSources parsed = parser.parse(cloned.path());
			int classCount = sourceWriter.write(analysis.getId(), parsed);
			analysis.recordParse(classCount, parsed.failedFiles());
			analysis.complete();
		}
		catch (CloneFailedException e) {
			analysis.fail(e.getMessage());
		}
		return repository.save(analysis);
	}

}
