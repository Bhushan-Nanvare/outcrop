package dev.outcrop.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.outcrop.clone.CloneFailedException;
import dev.outcrop.clone.ClonedRepository;
import dev.outcrop.clone.RepositoryCloner;
import dev.outcrop.parse.JavaSourceParser;
import dev.outcrop.parse.ParsedSources;
import dev.outcrop.source.SourceWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

	@Mock
	private AnalysisRepository repository;

	@Mock
	private RepositoryCloner cloner;

	@Mock
	private JavaSourceParser parser;

	@Mock
	private SourceWriter sourceWriter;

	@InjectMocks
	private AnalysisService service;

	@Test
	void createsAnalysisInPendingState() {
		when(repository.save(any(Analysis.class))).thenAnswer(call -> call.getArgument(0));

		Analysis analysis = service.create("https://github.com/Bhushan-Nanvare/outcrop");

		assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.PENDING);
		assertThat(analysis.getCreatedAt()).isNotNull();
		assertThat(analysis.getStartedAt()).isNull();
	}

	@Test
	void throwsWhenAnalysisIsMissing() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(id))
				.isInstanceOf(AnalysisNotFoundException.class)
				.hasMessageContaining(id.toString());
	}

	@Test
	void listsNewestFirst() {
		Analysis older = new Analysis("https://github.com/acme/older");
		Analysis newer = new Analysis("https://github.com/acme/newer");
		when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newer, older));

		assertThat(service.list()).containsExactly(newer, older);
	}

	@Test
	void runClonesParsesAndCompletes() {
		Analysis analysis = new Analysis("https://github.com/acme/repo");
		when(repository.findById(analysis.getId())).thenReturn(Optional.of(analysis));
		when(repository.save(any(Analysis.class))).thenAnswer(call -> call.getArgument(0));
		when(cloner.cloneRepository(anyString()))
				.thenReturn(new ClonedRepository(Path.of("no-such-directory"), "a".repeat(40), 42));
		when(parser.parse(any(Path.class))).thenReturn(new ParsedSources(List.of(), 3));
		when(sourceWriter.write(any(UUID.class), any(ParsedSources.class))).thenReturn(7);

		Analysis result = service.run(analysis.getId());

		assertThat(result.getStatus()).isEqualTo(AnalysisStatus.COMPLETE);
		assertThat(result.getCommitSha()).isEqualTo("a".repeat(40));
		assertThat(result.getJavaFileCount()).isEqualTo(42);
		assertThat(result.getClassCount()).isEqualTo(7);
		assertThat(result.getParseFailureCount()).isEqualTo(3);
		assertThat(result.getFinishedAt()).isNotNull();
	}

	@Test
	void runMarksAnalysisFailedWhenCloneFails() {
		Analysis analysis = new Analysis("https://github.com/acme/repo");
		when(repository.findById(analysis.getId())).thenReturn(Optional.of(analysis));
		when(repository.save(any(Analysis.class))).thenAnswer(call -> call.getArgument(0));
		when(cloner.cloneRepository(anyString())).thenThrow(new CloneFailedException("repository not found"));

		Analysis result = service.run(analysis.getId());

		assertThat(result.getStatus()).isEqualTo(AnalysisStatus.FAILED);
		assertThat(result.getFailureReason()).isEqualTo("repository not found");
		assertThat(result.getClassCount()).isNull();
		verify(sourceWriter, never()).write(any(UUID.class), any(ParsedSources.class));
	}

}
