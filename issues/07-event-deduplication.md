## 작업 내용

Kafka Metric 이벤트의 중복 수신을 방지하여 Metric이 중복 집계되지 않도록 한다.

## 요구사항

- 동일 이벤트 UUID 또는 비즈니스 키로 중복 처리를 차단한다
- At-least-once Kafka 전달을 전제로 멱등성을 보장한다
- 중복 이벤트는 WARN 로그 후 무시한다

## 작업 상세

- [ ] 처리 이력 Entity / Repository 구현 (event_uuid 기준)
- [ ] Consumer 또는 Application Service에서 중복 검사
- [ ] 멱등 처리 로직 구현
- [ ] Test 작성

## 참고사항

- Outbox 중복 방지 패턴(`existsByEventUuid`) 참고
- Redis는 Source of Truth로 사용하지 않는다
