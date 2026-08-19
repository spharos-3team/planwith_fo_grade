package com.planwith.planwith_fo_grade.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GradeQueryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Test
	void listsAllGradesFromDatabase() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());

		mockMvc.perform(get("/api/grade/grades"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(6))
				.andExpect(jsonPath("$.data[0].gradeCode").value("ROOKIE"))
				.andExpect(jsonPath("$.data[0].gradeName").value("🌱 새싹"))
				.andExpect(jsonPath("$.data[0].gradeLevel").value(1))
				.andExpect(jsonPath("$.data[0].conditions.length()").value(0))
				.andExpect(jsonPath("$.data[0].benefits[0].benefitCode").value("MONTHLY_FREE_TOKEN"))
				.andExpect(jsonPath("$.data[2].gradeCode").value("TRAVELER"))
				.andExpect(jsonPath("$.data[2].conditions[0].metricType").value("STORY_COUNT"))
				.andExpect(jsonPath("$.data[2].conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data[2].conditions[1].thresholdValue").value(100))
				.andExpect(jsonPath("$.data[2].conditions[2].thresholdValue").value(500))
				.andExpect(jsonPath("$.data[3].gradeCode").value("EXPLORER"))
				.andExpect(jsonPath("$.data[3].gradeLevel").value(4));
	}
}
