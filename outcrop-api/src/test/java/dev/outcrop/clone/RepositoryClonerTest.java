package dev.outcrop.clone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositoryClonerTest {

	@TempDir
	Path temp;

	private String sourceUrl;

	@BeforeEach
	void createSourceRepository() throws GitAPIException, IOException {
		Path source = temp.resolve("source");
		Files.createDirectories(source.resolve("src"));
		Files.writeString(source.resolve("src/Hello.java"), "class Hello {}");
		Files.writeString(source.resolve("src/World.java"), "class World {}");
		Files.writeString(source.resolve("README.md"), "not java");

		try (Git git = Git.init().setDirectory(source.toFile()).call()) {
			git.add().addFilepattern(".").call();
			git.commit().setMessage("initial").setSign(false).call();
		}
		sourceUrl = source.toUri().toString();
	}

	@Test
	void clonesRepositoryAndCountsJavaFiles() {
		RepositoryCloner cloner = new RepositoryCloner(properties(100, 250));

		try (ClonedRepository cloned = cloner.cloneRepository(sourceUrl)) {
			assertThat(cloned.javaFileCount()).isEqualTo(2);
			assertThat(cloned.commitSha()).hasSize(40);
			assertThat(cloned.path().resolve("src/Hello.java")).exists();
		}
	}

	@Test
	void deletesWorkingCopyOnClose() {
		RepositoryCloner cloner = new RepositoryCloner(properties(100, 250));
		Path cloneLocation;

		try (ClonedRepository cloned = cloner.cloneRepository(sourceUrl)) {
			cloneLocation = cloned.path();
			assertThat(cloneLocation).exists();
		}

		assertThat(cloneLocation).doesNotExist();
	}

	@Test
	void rejectsRepositoryOverTheJavaFileLimit() {
		RepositoryCloner cloner = new RepositoryCloner(properties(1, 250));

		assertThatThrownBy(() -> cloner.cloneRepository(sourceUrl))
				.isInstanceOf(CloneFailedException.class)
				.hasMessageContaining("2 Java files")
				.hasMessageContaining("limit is 1");
	}

	@Test
	void reportsFailureForUnreachableRepository() {
		RepositoryCloner cloner = new RepositoryCloner(properties(100, 250));

		assertThatThrownBy(() -> cloner.cloneRepository(temp.resolve("missing").toUri().toString()))
				.isInstanceOf(CloneFailedException.class)
				.hasMessageContaining("Could not clone");
	}

	private CloneProperties properties(int maxJavaFiles, int maxSizeMb) {
		return new CloneProperties(temp.resolve("workspace"), maxJavaFiles, maxSizeMb, 30);
	}

}
