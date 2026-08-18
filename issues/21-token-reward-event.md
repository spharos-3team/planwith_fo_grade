## 작업 내용

토큰 보상 지급 시 `GradeRewardGranted` 이벤트를 Transactional Outbox로 발행한다.

## 요구사항

- 비즈니스 DB 변경(보상 이력 INSERT) + Outbox INSERT가 동일 Transaction
- Kafka payload는 별도 JSON DTO (JPA Entity 직렬화 금지)
- Outbox Relay → `planwith.grade.reward-granted` 토픽

## 작업 상세

- [ ] GradeRewardGranted Event payload DTO 정의
- [ ] `GrantGradeRewardUseCase`에서 Outbox INSERT 연동
- [ ] `GradeEventType.GRADE_REWARD_GRANTED` 발행 검증
- [ ] Test 작성

## 참고사항

- #10 GradeChanged Outbox 패턴과 동일
- 토픽: `planwith.grade.reward-granted`
