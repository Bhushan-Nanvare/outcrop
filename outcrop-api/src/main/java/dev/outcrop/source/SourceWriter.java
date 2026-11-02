package dev.outcrop.source;

import dev.outcrop.parse.ParsedSources;
import dev.outcrop.parse.ParsedSources.ParsedClass;
import dev.outcrop.parse.ParsedSources.ParsedMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SourceWriter {

	private final SourceClassRepository classes;

	private final SourceMethodRepository methods;

	SourceWriter(SourceClassRepository classes, SourceMethodRepository methods) {
		this.classes = classes;
		this.methods = methods;
	}

	@Transactional
	public int write(UUID analysisId, ParsedSources parsed) {
		List<SourceClass> classEntities = new ArrayList<>();
		List<SourceMethod> methodEntities = new ArrayList<>();

		for (ParsedClass parsedClass : parsed.classes()) {
			SourceClass entity = SourceClass.from(analysisId, parsedClass);
			classEntities.add(entity);
			for (ParsedMethod method : parsedClass.methods()) {
				methodEntities.add(new SourceMethod(entity.getId(), method.name(), method.returnType(),
						method.parameterCount(), method.startLine()));
			}
		}

		classes.saveAll(classEntities);
		methods.saveAll(methodEntities);
		return classEntities.size();
	}

}
