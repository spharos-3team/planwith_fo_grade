## 작업 내용

Kafka Consumer Adapter에서 Metric 이벤트를 수신하고 Application Command로 변환한다.

## 요구사항

- Kafka Event는 Adapter를 통해 처리한다
- Consumer는 `grade.kafka.consumer-enabled` 설정으로 제어한다
- payload 파싱 실패 시 로그 및 재처리 전략을 고려한다

## 작업 상세

- [ ] `GradeInboundEventConsumer`에 이벤트 타입별 핸들러 연결
- [ ] Inbound Event DTO → Command 변환
- [ ] Application Service 호출
- [ ] Test 작성

## 참고사항

- 기존 Consumer 골격: `adapter.in.kafka.GradeInboundEventConsumer`
- EDA: REST 직접 조회보다 Kafka Event 기반 동기화 우선
