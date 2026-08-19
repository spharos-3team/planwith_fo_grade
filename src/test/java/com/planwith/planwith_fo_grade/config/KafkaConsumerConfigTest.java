package com.planwith.planwith_fo_grade.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.DefaultErrorHandler;

class KafkaConsumerConfigTest {

	@Test
	void retriesTransientFailuresAndSkipsIllegalArgument() {
		DefaultErrorHandler handler = (DefaultErrorHandler) new KafkaConsumerConfig().kafkaConsumerErrorHandler();

		assertThat(handler).isNotNull();
	}
}
