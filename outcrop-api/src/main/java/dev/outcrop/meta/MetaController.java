package dev.outcrop.meta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
class MetaController {

	private final String version;

	MetaController(@Value("${outcrop.version}") String version) {
		this.version = version;
	}

	@GetMapping
	ServiceInfo info() {
		return new ServiceInfo("outcrop", version);
	}

}
