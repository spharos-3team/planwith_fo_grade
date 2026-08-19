package com.planwith.planwith_fo_grade.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.port.in.GetMyGradeManagementQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeQueryCachePort;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeQueryCacheFallbackIntegrationTest {

	@Autowired
	private GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Test
	void returnsMysqlGradeManagementWhenRedisIsDown() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.ROOKIE).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				LocalDateTime.of(2026, 8, 19, 3, 0)
		));

		GradeManagementView view = getMyGradeManagementQueryUseCase.get(memberUuid);

		assertThat(view.currentGrade().code()).isEqualTo("ROOKIE");
		assertThat(view.currentBenefits().monthlyTokenAmount()).isEqualTo(10);
		assertThat(view.nextGrade().code()).isEqualTo("LEAF");
	}

	@TestConfiguration
	static class FailingRedisConfig {

		@Bean
		@Primary
		GradeQueryCachePort failingRedisCache() {
			return new GradeQueryCachePort() {
				@Override
				public Optional<GradeManagementView> findByMemberUuid(String memberUuid) {
					throw new RuntimeException("Redis unavailable");
				}

				@Override
				public void save(String memberUuid, GradeManagementView view) {
					throw new RuntimeException("Redis unavailable");
				}

				@Override
				public void evict(String memberUuid) {
					throw new RuntimeException("Redis unavailable");
				}
			};
		}
	}
}
