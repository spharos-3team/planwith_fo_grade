## 작업 내용

회원의 현재 Metric과 등급 기준을 기반으로 다음 등급을 계산하는 Query 로직을 구현한다.

## 요구사항

- 최상위 등급인 경우 다음 등급은 null 또는 동일 등급으로 처리
- 등급 기준 데이터를 참조하여 계산한다
- Query Side에서 Command Side DB를 변경하지 않는다

## 작업 상세

- [ ] 다음 등급 계산 Domain / Application 로직
- [ ] Query UseCase에 `nextGradeCode` 반영
- [ ] Web Response DTO에 다음 등급 정보 포함
- [ ] Test 작성

## 참고사항

- `MemberGradeView.nextGradeCode` 필드 활용
