## 작업 내용

grade-service의 API, Kafka, Outbox, Redis 연동에 대한 통합 테스트를 작성한다.

## 요구사항

- H2 또는 Testcontainers(MySQL, Kafka, Redis) 활용
- `@SpringBootTest` + `@ActiveProfiles("test")`
- API Test, Outbox E2E, Cache Aside fallback 시나리오 포함

## 작업 상세

- [ ] Query API 통합 테스트 (MockMvc)
- [ ] Metric 이벤트 수신 → Metric 갱신 → 등급 평가 E2E
- [ ] GradeChanged Outbox → Kafka 발행 E2E
- [ ] Redis Cache Aside + MySQL fallback 통합 테스트
- [ ] `./gradlew clean test`, `./gradlew build` 통과

## 참고사항

- 기존: `DeployControllerIntegrationTests`, `GradeEventOutboxAdapterIntegrationTest`
- Testcontainers BOM 2.0.5 사용
