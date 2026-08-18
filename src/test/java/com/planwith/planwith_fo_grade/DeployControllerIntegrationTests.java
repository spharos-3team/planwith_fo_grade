package com.planwith.planwith_fo_grade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeployControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deployCheckReturnsMarker() throws Exception {
		mockMvc.perform(get("/api/grade/deploy-check"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("grade-service"))
				.andExpect(jsonPath("$.marker").value("planwith-fo-grade-deploy-v1"))
				.andExpect(jsonPath("$.message").value("grade-service deploy pipeline ok"));
	}
}
