package dev.outcrop.parse;

import static org.assertj.core.api.Assertions.assertThat;

import dev.outcrop.parse.ParsedSources.ParsedClass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaSourceParserTest {

	private final JavaSourceParser parser = new JavaSourceParser();

	@TempDir
	Path sources;

	@Test
	void extractsPackageAnnotationsImportsAndMethods() throws IOException {
		write("com/acme/OrderController.java", """
				package com.acme;

				import java.util.List;
				import org.springframework.web.bind.annotation.RestController;

				@RestController
				public class OrderController {

					public List<String> list() {
						return List.of();
					}

					void place(String id, int quantity) {
					}
				}
				""");

		ParsedSources parsed = parser.parse(sources);

		assertThat(parsed.failedFiles()).isZero();
		assertThat(parsed.classes()).hasSize(1);

		ParsedClass parsedClass = parsed.classes().getFirst();
		assertThat(parsedClass.packageName()).isEqualTo("com.acme");
		assertThat(parsedClass.simpleName()).isEqualTo("OrderController");
		assertThat(parsedClass.kind()).isEqualTo(ClassKind.CLASS);
		assertThat(parsedClass.filePath()).isEqualTo("com/acme/OrderController.java");
		assertThat(parsedClass.annotations()).containsExactly("RestController");
		assertThat(parsedClass.imports()).containsExactly("java.util.List",
				"org.springframework.web.bind.annotation.RestController");
		assertThat(parsedClass.methods()).extracting(ParsedSources.ParsedMethod::name)
				.containsExactly("list", "place");
		assertThat(parsedClass.methods().getFirst().returnType()).isEqualTo("List<String>");
		assertThat(parsedClass.methods().getLast().parameterCount()).isEqualTo(2);
	}

	@Test
	void distinguishesInterfacesEnumsAndRecords() throws IOException {
		write("Repo.java", "package a; interface Repo {}");
		write("Status.java", "package a; enum Status { OPEN }");
		write("Point.java", "package a; record Point(int x, int y) {}");

		ParsedSources parsed = parser.parse(sources);

		assertThat(parsed.classes()).extracting(ParsedClass::simpleName, ParsedClass::kind)
				.containsExactlyInAnyOrder(
						org.assertj.core.groups.Tuple.tuple("Repo", ClassKind.INTERFACE),
						org.assertj.core.groups.Tuple.tuple("Status", ClassKind.ENUM),
						org.assertj.core.groups.Tuple.tuple("Point", ClassKind.RECORD));
	}

	@Test
	void countsUnparseableFilesWithoutFailingTheRun() throws IOException {
		write("Good.java", "package a; class Good {}");
		write("Broken.java", "package a; class Broken { this is not java");

		ParsedSources parsed = parser.parse(sources);

		assertThat(parsed.failedFiles()).isEqualTo(1);
		assertThat(parsed.classes()).extracting(ParsedClass::simpleName).containsExactly("Good");
	}

	@Test
	void ignoresFilesWithNoPackage() throws IOException {
		write("Loose.java", "class Loose {}");

		ParsedSources parsed = parser.parse(sources);

		assertThat(parsed.classes().getFirst().packageName()).isEmpty();
	}

	private void write(String relativePath, String content) throws IOException {
		Path file = sources.resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.writeString(file, content);
	}

}
