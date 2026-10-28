package dev.outcrop.analysis;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses")
class AnalysisController {

	private final AnalysisService service;

	AnalysisController(AnalysisService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<AnalysisResponse> create(@Valid @RequestBody CreateAnalysisRequest request) {
		Analysis analysis = service.create(request.repositoryUrl());
		return ResponseEntity.created(URI.create("/api/analyses/" + analysis.getId()))
				.body(AnalysisResponse.from(analysis));
	}

	@GetMapping("/{id}")
	AnalysisResponse get(@PathVariable UUID id) {
		return AnalysisResponse.from(service.get(id));
	}

	@GetMapping
	List<AnalysisResponse> list() {
		return service.list().stream().map(AnalysisResponse::from).toList();
	}

}
