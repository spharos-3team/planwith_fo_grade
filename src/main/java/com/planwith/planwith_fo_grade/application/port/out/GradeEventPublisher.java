package com.planwith.planwith_fo_grade.application.port.out;

import java.util.concurrent.CompletableFuture;

public interface GradeEventPublisher {

	CompletableFuture<Void> publish(String topic, String key, String payload);
}
