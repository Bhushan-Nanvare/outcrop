package dev.outcrop.clone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.springframework.stereotype.Component;

@Component
public class RepositoryCloner {

	private final CloneProperties properties;

	RepositoryCloner(CloneProperties properties) {
		this.properties = properties;
	}

	public ClonedRepository cloneRepository(String repositoryUrl) {
		Path target = properties.workspace().resolve(UUID.randomUUID().toString());
		try {
			Files.createDirectories(properties.workspace());
			try (Git git = Git.cloneRepository()
					.setURI(repositoryUrl)
					.setDirectory(target.toFile())
					.setDepth(1)
					.setTimeout(properties.timeoutSeconds())
					.call()) {
				String commitSha = git.getRepository().resolve(Constants.HEAD).getName();
				int javaFiles = countJavaFiles(target);
				checkWithinLimits(javaFiles, sizeInBytes(target));
				return new ClonedRepository(target, commitSha, javaFiles);
			}
		}
		catch (Exception e) {
			ClonedRepository.deleteRecursively(target);
			throw e instanceof CloneFailedException failure ? failure
					: new CloneFailedException("Could not clone " + repositoryUrl + ": " + e.getMessage());
		}
	}

	private void checkWithinLimits(int javaFiles, long sizeBytes) {
		if (javaFiles > properties.maxJavaFiles()) {
			throw new CloneFailedException(
					"Repository has " + javaFiles + " Java files, limit is " + properties.maxJavaFiles());
		}
		long sizeMb = sizeBytes / (1024 * 1024);
		if (sizeMb > properties.maxSizeMb()) {
			throw new CloneFailedException(
					"Repository is " + sizeMb + " MB, limit is " + properties.maxSizeMb() + " MB");
		}
	}

	private int countJavaFiles(Path root) throws IOException {
		try (Stream<Path> files = Files.walk(root)) {
			return (int) files.filter(Files::isRegularFile)
					.filter(file -> file.toString().endsWith(".java"))
					.count();
		}
	}

	private long sizeInBytes(Path root) throws IOException {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(Files::isRegularFile).mapToLong(this::fileSize).sum();
		}
	}

	private long fileSize(Path file) {
		try {
			return Files.size(file);
		}
		catch (IOException e) {
			return 0;
		}
	}

}
