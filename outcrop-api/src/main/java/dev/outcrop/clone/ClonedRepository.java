package dev.outcrop.clone;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public record ClonedRepository(Path path, String commitSha, int javaFileCount) implements AutoCloseable {

	@Override
	public void close() {
		deleteRecursively(path);
	}

	static void deleteRecursively(Path directory) {
		if (!Files.exists(directory)) {
			return;
		}
		try (Stream<Path> entries = Files.walk(directory)) {
			for (Path entry : entries.sorted(Comparator.reverseOrder()).toList()) {
				entry.toFile().setWritable(true);
				Files.delete(entry);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("Could not delete " + directory, e);
		}
	}

}
