package com.planwith.planwith_fo_grade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_grade.config.DeployProperties;
import com.planwith.planwith_fo_grade.config.GradeCacheProperties;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.config.GradeOutboxProperties;
import com.planwith.planwith_fo_grade.config.GradeRewardProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		DeployProperties.class,
		GradeKafkaProperties.class,
		GradeOutboxProperties.class,
		GradeCacheProperties.class,
		GradeRewardProperties.class
})
public class PlanwithFoGradeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoGradeApplication.class, args);
	}
}
