## 작업 내용

회원의 현재 등급 정보를 조회하는 Query API를 구현한다.

## 요구사항

- `member_uuid`로 조회한다
- 등급 정보가 없으면 `GradeNotFoundException` 처리
- Query Side는 Command Side와 분리한다

## 작업 상세

- [ ] `GetMemberGradeQueryUseCase` 구현체 작성
- [ ] Web Controller + Response DTO
- [ ] Persistence Query Adapter
- [ ] Test 작성

## 참고사항

- `GetMemberGradeQuery`, `MemberGradeView` 사용
- Redis 캐시는 #18에서 연동
