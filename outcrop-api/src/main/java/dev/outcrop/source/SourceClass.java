package dev.outcrop.source;

import dev.outcrop.parse.ClassKind;
import dev.outcrop.parse.ParsedSources.ParsedClass;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "source_class")
public class SourceClass {

	@Id
	private UUID id;

	@Column(name = "analysis_id", nullable = false)
	private UUID analysisId;

	@Column(name = "package_name", nullable = false)
	private String packageName;

	@Column(name = "simple_name", nullable = false)
	private String simpleName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ClassKind kind;

	@Column(name = "file_path", nullable = false)
	private String filePath;

	@ElementCollection
	@CollectionTable(name = "source_class_annotation", joinColumns = @JoinColumn(name = "source_class_id"))
	@Column(name = "annotation", nullable = false)
	private List<String> annotations;

	@ElementCollection
	@CollectionTable(name = "source_class_import", joinColumns = @JoinColumn(name = "source_class_id"))
	@Column(name = "imported", nullable = false)
	private List<String> imports;

	protected SourceClass() {
	}

	private SourceClass(UUID analysisId, ParsedClass parsed) {
		this.id = UUID.randomUUID();
		this.analysisId = analysisId;
		this.packageName = parsed.packageName();
		this.simpleName = parsed.simpleName();
		this.kind = parsed.kind();
		this.filePath = parsed.filePath();
		this.annotations = parsed.annotations();
		this.imports = parsed.imports();
	}

	public static SourceClass from(UUID analysisId, ParsedClass parsed) {
		return new SourceClass(analysisId, parsed);
	}

	public String qualifiedName() {
		return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAnalysisId() {
		return analysisId;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public ClassKind getKind() {
		return kind;
	}

	public String getFilePath() {
		return filePath;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public List<String> getImports() {
		return imports;
	}

}
