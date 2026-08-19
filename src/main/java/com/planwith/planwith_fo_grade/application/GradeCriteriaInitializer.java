package com.planwith.planwith_fo_grade.application;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;

@Component
@ConditionalOnProperty(name = "grade.criteria.seed-enabled", havingValue = "true", matchIfMissing = true)
public class GradeCriteriaInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(GradeCriteriaInitializer.class);

	private final GradeCriteriaPort gradeCriteriaPort;

	public GradeCriteriaInitializer(GradeCriteriaPort gradeCriteriaPort) {
		this.gradeCriteriaPort = gradeCriteriaPort;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		log.info("GradeCriteriaInitializer : run : 등급 기준 초기 데이터 동기화 시작");
		int createdCount = 0;
		int updatedCount = 0;
		for (Grade catalogGrade : GradeCriteriaCatalog.initialGrades()) {
			Optional<Grade> existing = gradeCriteriaPort.findByGradeCode(catalogGrade.gradeCode());
			if (existing.isEmpty()) {
				Grade saved = gradeCriteriaPort.save(catalogGrade);
				createdCount++;
				log.info(
						"GradeCriteriaInitializer : run : 등급 기준 적재 완료 - gradeCode={}, gradeLevel={}",
						saved.gradeCode(),
						saved.gradeLevel()
				);
				continue;
			}
			Grade saved = gradeCriteriaPort.save(withPersistedId(existing.get(), catalogGrade));
			updatedCount++;
			log.info(
					"GradeCriteriaInitializer : run : 등급 기준 동기화 완료 - gradeCode={}, benefitCount={}",
					saved.gradeCode(),
					saved.benefits().size()
			);
		}
		log.info(
				"GradeCriteriaInitializer : run : 등급 기준 초기 데이터 동기화 완료 - createdCount={}, updatedCount={}",
				createdCount,
				updatedCount
		);
	}

	private static Grade withPersistedId(Grade existing, Grade catalogGrade) {
		if (existing.gradeId() == null) {
			return catalogGrade;
		}
		return Grade.reconstitute(
				existing.gradeId(),
				catalogGrade.gradeCode(),
				catalogGrade.gradeName(),
				catalogGrade.gradeLevel(),
				catalogGrade.description(),
				catalogGrade.conditions(),
				catalogGrade.benefits()
		);
	}
}
