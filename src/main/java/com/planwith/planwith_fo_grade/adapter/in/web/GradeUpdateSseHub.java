package com.planwith.planwith_fo_grade.adapter.in.web;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.planwith.planwith_fo_grade.application.port.out.GradeUpdateNotificationPort;

@Component
public class GradeUpdateSseHub implements GradeUpdateNotificationPort {

	private static final Logger log = LoggerFactory.getLogger(GradeUpdateSseHub.class);
	private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;

	private final ConcurrentMap<String, Set<SseEmitter>> emittersByMember = new ConcurrentHashMap<>();

	public SseEmitter subscribe(String memberUuid) {
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
		emittersByMember.computeIfAbsent(memberUuid, ignored -> new CopyOnWriteArraySet<>()).add(emitter);

		emitter.onCompletion(() -> remove(memberUuid, emitter));
		emitter.onTimeout(() -> {
			remove(memberUuid, emitter);
			emitter.complete();
		});
		emitter.onError(exception -> remove(memberUuid, emitter));

		try {
			emitter.send(SseEmitter.event()
					.name("connected")
					.data(new GradeUpdateNotification(memberUuid, Instant.now())));
			log.info("GradeUpdateSseHub : subscribe : 등급 실시간 알림 구독 완료 - memberUuid={}", memberUuid);
		} catch (IOException | IllegalStateException exception) {
			remove(memberUuid, emitter);
			emitter.completeWithError(exception);
			log.warn("GradeUpdateSseHub : subscribe : 등급 실시간 알림 구독 초기화 실패 - memberUuid={}", memberUuid);
		}
		return emitter;
	}

	@Override
	public void notifyUpdated(String memberUuid) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					sendUpdated(memberUuid);
				}
			});
			return;
		}
		sendUpdated(memberUuid);
	}

	@Scheduled(fixedDelay = 20_000L)
	public void sendHeartbeat() {
		emittersByMember.forEach((memberUuid, emitters) ->
				emitters.forEach(emitter -> send(memberUuid, emitter, SseEmitter.event().comment("heartbeat")))
		);
	}

	private void sendUpdated(String memberUuid) {
		Set<SseEmitter> emitters = emittersByMember.get(memberUuid);
		if (emitters == null || emitters.isEmpty()) {
			return;
		}

		GradeUpdateNotification notification = new GradeUpdateNotification(memberUuid, Instant.now());
		emitters.forEach(emitter -> send(
				memberUuid,
				emitter,
				SseEmitter.event().name("grade-updated").data(notification)
		));
		log.info("GradeUpdateSseHub : sendUpdated : 등급 집계 변경 알림 전송 - memberUuid={}, subscriberCount={}",
				memberUuid, emitters.size());
	}

	private void send(String memberUuid, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
		try {
			emitter.send(event);
		} catch (IOException | IllegalStateException exception) {
			remove(memberUuid, emitter);
			emitter.completeWithError(exception);
			log.debug("GradeUpdateSseHub : send : 종료된 등급 실시간 알림 연결 정리 - memberUuid={}", memberUuid);
		}
	}

	private void remove(String memberUuid, SseEmitter emitter) {
		emittersByMember.computeIfPresent(memberUuid, (ignored, emitters) -> {
			emitters.remove(emitter);
			return emitters.isEmpty() ? null : emitters;
		});
	}

	private record GradeUpdateNotification(String memberUuid, Instant occurredAt) {
	}
}
