## 작업 내용

월간 토큰 보상 대상 회원을 계산하는 Command Side 기능을 구현한다.

## 요구사항

- 등급별 월간 토큰 보상 정책을 등급 기준 데이터에서 참조한다
- 보상 대상 선정 기준(등급, 활동 Metric 등)을 명확히 한다
- Application Service에서 트랜잭션 경계를 관리한다

## 작업 상세

- [ ] 월간 보상 대상 계산 Domain / Application Service
- [ ] 보상 대상 선정 Command / UseCase
- [ ] 스케줄 또는 이벤트 트리거 진입점 설계
- [ ] Test 작성

## 참고사항

- CQRS Command Side
- #20 보상 이력 / 중복 방지, #21 토큰 보상 이벤트와 연계
