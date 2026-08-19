package com.planwith.planwith_fo_grade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grade.reward")
public class GradeRewardProperties {

	private boolean monthlyEnabled = true;
	private String monthlyCron = "0 0 0 1 * *";

	public boolean isMonthlyEnabled() {
		return monthlyEnabled;
	}

	public void setMonthlyEnabled(boolean monthlyEnabled) {
		this.monthlyEnabled = monthlyEnabled;
	}

	public String getMonthlyCron() {
		return monthlyCron;
	}

	public void setMonthlyCron(String monthlyCron) {
		this.monthlyCron = monthlyCron;
	}
}
