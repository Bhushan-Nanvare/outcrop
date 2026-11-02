package dev.outcrop.clone;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outcrop.clone")
public record CloneProperties(Path workspace, int maxJavaFiles, int maxSizeMb, int timeoutSeconds) {
}
