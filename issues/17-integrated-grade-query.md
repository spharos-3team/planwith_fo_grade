## 작업 내용

현재 등급, 다음 등급, 남은 조건, 달성률, 현재 혜택을 하나의 API로 통합 조회하는 Query API를 구현한다.

## 요구사항

- #12~#16 Query 로직을 하나의 UseCase로 조합한다
- `MemberGradeView` 전체 필드를 채워 반환한다
- Controller는 UseCase 호출만 담당한다

## 작업 상세

- [ ] 통합 Query UseCase / Application Service 구현
- [ ] Web Controller + Response DTO
- [ ] API 엔드포인트 설계 (`GET /api/grade/members/{memberUuid}` 등)
- [ ] Test 작성

## 참고사항

- CQRS Query Side
- Redis Cache Aside는 #18에서 연동
