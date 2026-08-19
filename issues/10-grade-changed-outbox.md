## 작업 내용

등급 변경 시 외부 서비스에 알릴 `GradeChanged` 이벤트를 Transactional Outbox Pattern으로 발행한다.

## 요구사항

- 비즈니스 DB 변경 + Outbox INSERT가 동일 MySQL Transaction 안에서 수행된다
- Kafka payload는 JPA Entity가 아닌 별도 JSON DTO를 사용한다
- Outbox Relay가 Kafka로 발행한다

## 작업 상세

- [x] GradeChanged Event payload DTO 정의
- [x] 등급 변경 Application Service에서 Outbox INSERT 연동
- [x] `eventType = GradeChanged` 토픽 발행 검증
- [x] Test 작성

## 참고사항

- 기존 Outbox: `grade_outbox` 테이블, `GradeOutboxRelay`
- 토픽: `planwith.grade.changed`
