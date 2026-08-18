## 작업 내용

회원 등급 Metric(스토리 수, 팔로워 수, 좋아요 수 등)을 갱신하는 Command Side 기능을 구현한다.

## 요구사항

- Transaction 경계는 Application Service에서 관리한다
- Metric 갱신 후 등급 평가 트리거 가능하도록 설계한다
- 회원별 Metric은 grade-service DB에만 저장한다

## 작업 상세

- [ ] `RecordGradeMetricUseCase` 구현체 작성
- [ ] MemberMetric Entity / Repository Adapter 구현
- [ ] Metric delta 적용 로직 구현
- [ ] Test 작성

## 참고사항

- CQRS Command Side
- `RecordGradeMetricCommand` 사용
