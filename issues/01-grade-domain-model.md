## 작업 내용

grade-service의 등급 도메인 모델을 정의한다.
등급 코드, 등급명, 승급 조건, 혜택 등 핵심 도메인 개념을 domain 계층에 구현한다.

## 요구사항

- domain 계층은 Spring/JPA/Kafka/Redis에 직접 의존하지 않는다
- 서비스 내부 PK는 BIGINT, 서비스 간 식별자는 *_uuid를 사용한다
- 등급 변경 규칙과 상태 전이는 도메인 객체 또는 도메인 서비스에 위치한다

## 작업 상세

- [ ] `Grade` / `GradeTier` 등 핵심 도메인 모델 정의
- [ ] `MemberGrade` 도메인 모델 정의 (회원별 현재 등급 상태)
- [ ] 도메인 불변 규칙 및 상태 전이 메서드 구현
- [ ] 도메인 예외 보완 (필요 시)
- [ ] Test 작성

## 참고사항

- Hexagonal Architecture: `domain.model`, `domain.service`
- 선행: 프로젝트 기본 셋팅 (#3) 완료
- ERD 테이블/컬럼명 임의 변경 금지
