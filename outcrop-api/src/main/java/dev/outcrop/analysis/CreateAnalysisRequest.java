package dev.outcrop.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAnalysisRequest(

		@NotBlank
		@Pattern(regexp = "https://github\\.com/[\\w.-]+/[\\w.-]+?(\\.git)?",
				message = "must be an https github repository url")
		String repositoryUrl) {
}
