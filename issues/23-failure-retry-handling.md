## 작업 내용

Outbox Relay 실패, Kafka Consumer 재처리, Redis 장애 등 장애 상황에 대한 재처리 및 복구 전략을 구현한다.

## 요구사항

- Outbox `retry_count` 증가 및 재시도 정책 적용
- Kafka Consumer 처리 실패 시 offset/commit 전략 검토
- Redis 장애 시 MySQL fallback (Cache miss 처리)
- ERROR/WARN 로그로 장애 추적 가능

## 작업 상세

- [ ] Outbox Relay 재시도 정책 보완 (max retry, backoff)
- [ ] Kafka Consumer error handler / DLQ 전략 (필요 시)
- [ ] Redis fallback 검증 및 로그 보완
- [ ] Test 작성

## 참고사항

- 기존: `GradeOutboxRelay.increaseRetryCount()`
- 운영 환경 장애 대응 가이드 문서화 권장
