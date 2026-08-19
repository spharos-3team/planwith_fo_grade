package com.planwith.planwith_fo_grade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConsumerConfig {

	private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

	@Bean
	CommonErrorHandler kafkaConsumerErrorHandler() {
		ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0d);
		backOff.setMaxInterval(60_000L);
		DefaultErrorHandler handler = new DefaultErrorHandler(
				(record, exception) -> log.error(
						"KafkaConsumerConfig : kafkaConsumerErrorHandler : 재시도 불가능한 Kafka 이벤트라 건너뜀 - topic={}, partition={}, offset={}",
						record.topic(),
						record.partition(),
						record.offset()
				),
				backOff
		);
		handler.addNotRetryableExceptions(IllegalArgumentException.class);
		return handler;
	}
}
