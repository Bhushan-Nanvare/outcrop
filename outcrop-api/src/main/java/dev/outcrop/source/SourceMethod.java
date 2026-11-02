package dev.outcrop.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "source_method")
public class SourceMethod {

	@Id
	private UUID id;

	@Column(name = "source_class_id", nullable = false)
	private UUID sourceClassId;

	@Column(nullable = false)
	private String name;

	@Column(name = "return_type", nullable = false)
	private String returnType;

	@Column(name = "parameter_count", nullable = false)
	private int parameterCount;

	@Column(name = "start_line", nullable = false)
	private int startLine;

	protected SourceMethod() {
	}

	SourceMethod(UUID sourceClassId, String name, String returnType, int parameterCount, int startLine) {
		this.id = UUID.randomUUID();
		this.sourceClassId = sourceClassId;
		this.name = name;
		this.returnType = returnType;
		this.parameterCount = parameterCount;
		this.startLine = startLine;
	}

	public UUID getId() {
		return id;
	}

	public UUID getSourceClassId() {
		return sourceClassId;
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public int getParameterCount() {
		return parameterCount;
	}

	public int getStartLine() {
		return startLine;
	}

}
