package dev.outcrop.analysis;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AnalysisService service;

	@Test
	void createReturnsCreatedWithLocation() throws Exception {
		Analysis analysis = new Analysis("https://github.com/acme/repo");
		when(service.create(anyString())).thenReturn(analysis);

		mockMvc.perform(post("/api/analyses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"repositoryUrl\":\"https://github.com/acme/repo\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/analyses/" + analysis.getId()))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void createRejectsNonGithubUrl() throws Exception {
		mockMvc.perform(post("/api/analyses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"repositoryUrl\":\"ftp://example.com/repo\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getReturnsNotFoundForUnknownId() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.get(id)).thenThrow(new AnalysisNotFoundException(id));

		mockMvc.perform(get("/api/analyses/{id}", id)).andExpect(status().isNotFound());
	}

}
