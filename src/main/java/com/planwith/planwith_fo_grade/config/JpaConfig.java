package com.planwith.planwith_fo_grade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.planwith.planwith_fo_grade.adapter.out.persistence")
public class JpaConfig {
}
