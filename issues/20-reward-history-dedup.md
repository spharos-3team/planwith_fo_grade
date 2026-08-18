## 작업 내용

등급 보상 지급 이력을 저장하고, 동일 회원/동일 기간/동일 보상 타입의 중복 지급을 방지한다.

## 요구사항

- 보상 이력 Entity를 grade-service DB에 저장한다
- 멱등 키(회원 UUID + 보상 기간 + 보상 타입)로 중복 지급 차단
- 중복 시 WARN 로그 후 skip

## 작업 상세

- [ ] 보상 이력 Entity / Repository Adapter
- [ ] `GrantGradeRewardUseCase` 구현체에 중복 검사
- [ ] 보상 이력 조회 Port (필요 시)
- [ ] Test 작성

## 참고사항

- #7 이벤트 중복 방지 패턴 참고
- `GrantGradeRewardCommand` 사용
