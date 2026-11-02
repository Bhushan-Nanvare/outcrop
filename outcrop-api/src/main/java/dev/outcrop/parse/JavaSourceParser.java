package dev.outcrop.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import dev.outcrop.parse.ParsedSources.ParsedClass;
import dev.outcrop.parse.ParsedSources.ParsedMethod;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class JavaSourceParser {

	private final JavaParser parser = new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21));

	public ParsedSources parse(Path root) {
		List<ParsedClass> classes = new ArrayList<>();
		int failedFiles = 0;

		for (Path file : javaFilesIn(root)) {
			ParseResult<CompilationUnit> result = parseFile(file);
			if (result.getResult().isEmpty() || !result.isSuccessful()) {
				failedFiles++;
				continue;
			}
			classes.addAll(toClasses(result.getResult().get(), relativePath(root, file)));
		}
		return new ParsedSources(classes, failedFiles);
	}

	private List<Path> javaFilesIn(Path root) {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(Files::isRegularFile)
					.filter(file -> file.toString().endsWith(".java"))
					.filter(file -> !file.toString().contains(".git"))
					.toList();
		}
		catch (IOException e) {
			throw new UncheckedIOException("Could not read sources under " + root, e);
		}
	}

	private ParseResult<CompilationUnit> parseFile(Path file) {
		try {
			return parser.parse(file);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Could not read " + file, e);
		}
	}

	private List<ParsedClass> toClasses(CompilationUnit unit, String filePath) {
		String packageName = unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse("");
		List<String> imports = unit.getImports().stream().map(ImportDeclaration::getNameAsString).toList();

		return unit.getTypes().stream()
				.map(type -> new ParsedClass(packageName, type.getNameAsString(), kindOf(type), filePath,
						annotationsOf(type), imports, methodsOf(type)))
				.toList();
	}

	private ClassKind kindOf(TypeDeclaration<?> type) {
		if (type instanceof ClassOrInterfaceDeclaration declaration) {
			return declaration.isInterface() ? ClassKind.INTERFACE : ClassKind.CLASS;
		}
		if (type instanceof EnumDeclaration) {
			return ClassKind.ENUM;
		}
		if (type instanceof RecordDeclaration) {
			return ClassKind.RECORD;
		}
		if (type instanceof AnnotationDeclaration) {
			return ClassKind.ANNOTATION;
		}
		return ClassKind.CLASS;
	}

	private List<String> annotationsOf(TypeDeclaration<?> type) {
		return type.getAnnotations().stream().map(AnnotationExpr::getNameAsString).toList();
	}

	private List<ParsedMethod> methodsOf(TypeDeclaration<?> type) {
		return type.getMethods().stream().map(this::toMethod).toList();
	}

	private ParsedMethod toMethod(MethodDeclaration method) {
		return new ParsedMethod(method.getNameAsString(), method.getType().asString(),
				method.getParameters().size(), method.getBegin().map(position -> position.line).orElse(0));
	}

	private String relativePath(Path root, Path file) {
		return root.relativize(file).toString().replace('\\', '/');
	}

}
