## 작업 내용

등급 평가 결과에 따라 회원의 등급을 변경하는 Command Side 기능을 구현한다.

## 요구사항

- 등급 변경은 Application Service `@Transactional` 안에서 수행한다
- 변경 이력을 저장한다
- Controller에 Business Logic을 작성하지 않는다

## 작업 상세

- [ ] `ChangeMemberGradeUseCase` 구현체 작성
- [ ] MemberGrade 상태 변경 Persistence Adapter
- [ ] 등급 변경 이력 Entity / Repository
- [ ] Test 작성

## 참고사항

- `ChangeMemberGradeCommand` 사용
- GradeChanged Outbox는 #10에서 연결
