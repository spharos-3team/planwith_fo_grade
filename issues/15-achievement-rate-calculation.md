## 작업 내용

현재 등급에서 다음 등급까지의 달성률(%)을 계산하는 Query 로직을 구현한다.

## 요구사항

- 0~100 범위의 달성률을 반환한다
- 최상위 등급인 경우 100% 또는 N/A 처리 정책을 명확히 한다
- Metric 복수 조건 시 가중치 또는 최소 달성률 정책을 적용한다

## 작업 상세

- [ ] 달성률 계산 Domain / Application 로직
- [ ] `MemberGradeView.achievementRate` 반영
- [ ] Web Response DTO 포함
- [ ] Test 작성

## 참고사항

- CQRS Query Side
- #13, #14와 연계
