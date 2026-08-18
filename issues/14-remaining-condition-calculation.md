## 작업 내용

다음 등급 승급까지 남은 조건(필요 Metric, 부족분)을 계산하는 Query 로직을 구현한다.

## 요구사항

- 등급 기준 데이터의 Metric 임계값과 현재 Metric 차이를 계산한다
- 조건이 복수인 경우 각 Metric별 남은량을 제공한다
- Query Side 전용 로직이다

## 작업 상세

- [ ] 남은 조건 계산 Application Service 로직
- [ ] `MemberGradeView.nextGradeCondition` 반영
- [ ] Web Response DTO 설계
- [ ] Test 작성

## 참고사항

- CQRS Query Side
- #13 다음 등급 계산과 함께 사용
