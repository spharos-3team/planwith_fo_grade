## 작업 내용

회원의 현재 Metric과 등급 기준 데이터를 비교하여 등급 평가 정책을 구현한다.

## 요구사항

- 등급 평가 로직은 domain.service 또는 Application Service에 위치한다
- 승급/강등 조건을 등급 기준 데이터 기반으로 판단한다
- 평가 결과로 등급 변경이 필요한지 여부를 반환한다

## 작업 상세

- [ ] `EvaluateGradeUseCase` 구현체 작성
- [ ] 등급 평가 Domain Service 구현
- [ ] Metric vs 등급 기준 비교 로직
- [ ] Test 작성

## 참고사항

- CQRS Command Side
- `EvaluateGradeCommand` 사용
