## 작업 내용

전체 등급표(모든 등급 티어, 승급 조건, 혜택 요약)를 조회하는 Query API를 구현한다.

## 요구사항

- Entity를 Controller Response로 직접 반환하지 않는다
- Web DTO / Application DTO / Persistence Entity 분리
- Controller에 Business Logic을 작성하지 않는다

## 작업 상세

- [ ] Query UseCase / Application Service 구현
- [ ] Web Controller + Response DTO 구현
- [ ] Persistence Query Adapter 구현
- [ ] Test 작성

## 참고사항

- CQRS Query Side
- API 경로는 Gateway 연동 시 `/api/grade/**` 하위
