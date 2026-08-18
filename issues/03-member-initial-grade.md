## 작업 내용

신규 회원 또는 등급 정보가 없는 회원에게 초기 등급을 부여하는 기능을 구현한다.

## 요구사항

- 회원 식별자는 `member_uuid`를 사용한다
- 초기 등급은 등급 기준 데이터의 최하위 등급으로 설정한다
- Application Service에서 트랜잭션 경계를 관리한다

## 작업 상세

- [ ] 회원 초기 등급 부여 Command / UseCase 정의
- [ ] Application Service 구현
- [ ] Persistence Adapter 구현 (MemberGrade 저장)
- [ ] Test 작성

## 참고사항

- Kafka 이벤트 또는 REST API 진입점은 후속 이슈에서 연결 가능
- Entity를 Controller Response로 직접 반환하지 않는다
