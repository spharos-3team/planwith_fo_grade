## 작업 내용

grade-service 핵심 Domain, Application Service, Policy 로직에 대한 단위 테스트를 작성한다.

## 요구사항

- Domain Service, 등급 평가 정책, 달성률 계산 등 순수 로직 위주
- Mock을 활용하여 Adapter 의존 없이 테스트 가능
- JUnit5 사용

## 작업 상세

- [ ] Domain Service 단위 테스트
- [ ] 등급 평가 / 달성률 / 남은 조건 계산 테스트
- [ ] Command Application Service 단위 테스트 (Mock Port)
- [ ] Query Application Service 단위 테스트 (Mock Port)

## 참고사항

- #1~#22 기능 구현 후 작성
- `./gradlew test` 통과 필수
