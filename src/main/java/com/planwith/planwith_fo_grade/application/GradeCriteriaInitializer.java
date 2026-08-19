package com.planwith.planwith_fo_grade.application;

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
		log.info("GradeCriteriaInitializer : run : 등급 기준 초기 데이터 적재 시작");
		int savedCount = 0;
		for (Grade grade : GradeCriteriaCatalog.initialGrades()) {
			if (gradeCriteriaPort.findByGradeCode(grade.gradeCode()).isPresent()) {
				log.debug(
						"GradeCriteriaInitializer : run : 이미 존재하는 등급은 건너뜀 - gradeCode={}",
						grade.gradeCode()
				);
				continue;
			}
			Grade saved = gradeCriteriaPort.save(grade);
			savedCount++;
			log.info(
					"GradeCriteriaInitializer : run : 등급 기준 적재 완료 - gradeCode={}, gradeLevel={}",
					saved.gradeCode(),
					saved.gradeLevel()
			);
		}
		if (savedCount == 0) {
			log.info("GradeCriteriaInitializer : run : 등급 기준 데이터가 이미 존재하여 적재를 생략");
			return;
		}
		log.info("GradeCriteriaInitializer : run : 등급 기준 초기 데이터 적재 완료 - savedCount={}", savedCount);
	}
}
