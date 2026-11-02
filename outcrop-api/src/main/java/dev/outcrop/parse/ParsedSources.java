package dev.outcrop.parse;

import java.util.List;

public record ParsedSources(List<ParsedClass> classes, int failedFiles) {

	public record ParsedClass(String packageName, String simpleName, ClassKind kind, String filePath,
			List<String> annotations, List<String> imports, List<ParsedMethod> methods) {
	}

	public record ParsedMethod(String name, String returnType, int parameterCount, int startLine) {
	}

}
