## 작업 내용

Story/Follow/Like 등 외부 서비스에서 발생하는 Metric 이벤트의 도메인 모델과 Application DTO를 정의한다.

## 요구사항

- Kafka payload는 JPA Entity를 그대로 직렬화하지 않는다
- 이벤트 타입별 Metric 종류를 명확히 구분한다
- domain 계층과 Adapter DTO를 분리한다

## 작업 상세

- [ ] Metric 이벤트 타입 enum / record 정의
- [ ] Inbound Event DTO (Adapter) 정의
- [ ] Application Command 변환 로직 설계
- [ ] Test 작성

## 참고사항

- 수신 대상: StoryCreated/Deleted, FollowCreated/Removed, LikeCreated/Removed
- `RecordGradeMetricCommand`와 연결
