package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.application.query.GradeBenefitView;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.application.query.GradeConditionView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementPageView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentMetricsView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.MetricProgressView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.NextGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.ProgressView;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;

class GetGradeManagementPageServiceTest {

	@Test
	void composesCatalogAndMemberGradeManagement() {
		List<GradeCatalogView> grades = List.of(
				new GradeCatalogView("ROOKIE", "🌱 새싹", 1, List.of(), List.of()),
				new GradeCatalogView("LEAF", "🧳 잎새", 2, List.of(), List.of())
		);
		GradeManagementView member = leafManagementView();
		GetGradeManagementPageService service = new GetGradeManagementPageService(
				() -> grades,
				memberUuid -> member
		);

		GradeManagementPageView view = service.get("member-uuid");

		assertThat(view.grades()).isEqualTo(grades);
		assertThat(view.member()).isEqualTo(member);
		assertThat(view.member().currentGrade().code()).isEqualTo("LEAF");
		assertThat(view.member().nextGrade().code()).isEqualTo("TRAVELER");
		assertThat(view.member().currentBenefits().monthlyTokenAmount()).isEqualTo(20);
	}

	@Test
	void doesNotLoadCatalogWhenMemberGradeIsMissing() {
		GetGradeManagementPageService service = new GetGradeManagementPageService(
				() -> {
					throw new AssertionError("전체 등급표는 회원 등급 조회 이후에 조합해야 한다.");
				},
				memberUuid -> {
					throw new GradeNotFoundException(memberUuid);
				}
		);

		assertThatThrownBy(() -> service.get("missing-member"))
				.isInstanceOf(GradeNotFoundException.class);
	}

	private static GradeManagementView leafManagementView() {
		return new GradeManagementView(
				new CurrentGradeView(
						"LEAF",
						"🧳 잎새",
						2,
						List.of(new GradeBenefitView("MONTHLY_FREE_TOKEN", "월간 무료 토큰", "20", "월 토큰"))
				),
				new CurrentMetricsView(7L, 62L, 410L),
				new NextGradeView(
						"TRAVELER",
						"✈️ 여행가",
						List.of(new GradeConditionView("STORY_COUNT", "스토리", 10L, "스토리 조건"))
				),
				new ProgressView(
						new MetricProgressView(7L, 10L, 3L, 70),
						new MetricProgressView(62L, 100L, 38L, 62),
						new MetricProgressView(410L, 500L, 90L, 82)
				),
				new CurrentBenefitSummaryView(
						"LEAF",
						"🧳 잎새",
						2,
						20,
						false,
						false,
						false,
						false,
						null
				)
		);
	}
}
