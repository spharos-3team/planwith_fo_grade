# Release Note / RM — planwith-fo-grade

## 1. 서비스 개요

| 항목 | 내용 |
|------|------|
| 서비스명 | `planwith-fo-grade` |
| 레포 | `planwith_fo_grade` |
| 포트 | `8083` |
| Eureka / Compose / ECR | `planwith-fo-grade` |
| 패키지 | `com.planwith.planwith_fo_grade` |
| DB | `grade_db` (로컬 yml은 `grade` 스키마명 혼용 주의) |
| 역할 | 등급 기준·회원 등급 부여/승급, 메트릭 집계, 월간 FREE 토큰 보상 이벤트 발행 |

등급 정책과 보상 **확정**은 Grade가 담당하고, 토큰 잔액은 건드리지 않습니다. Token은 `GradeRewardGranted`만 구독합니다.

---

## 2. 도메인 범위

### 2.1 Grade Criteria / Catalog

- 등급: `ROOKIE` → `LEAF` → `TRAVELER` → `EXPLORER` → `ADVENTURE` → `PLANWITH`
- 승급 메트릭(AND): 스토리 수 / 팔로워 수 / 받은 좋아요 수
- 월간 FREE 토큰(등급별): 10 / 20 / 30 / 50 / 70 / 120
- 혜택 코드: `MONTHLY_FREE_TOKEN`, `PROFILE_BADGE`, `MEMBERSHIP_*`, `PROFILE_SPECIAL_BORDER`, `NON_MEMBER_STORY_PRIORITY` 등  
  → 뱃지·멤버십 등은 이벤트/조회용, **실행은 타 서비스**

### 2.2 Grade Member / Metric

- 초기 부여: `MemberCreated` → ROOKIE (+ 가입월 토큰 보상 요청)
- 메트릭: 스토리 생성·삭제, 팔로우, 좋아요 이벤트 반영
- 재평가 후 등급 변경 시 `GradeChanged` Outbox
- 이벤트 `eventUuid` 멱등 (`processed_grade_event`)

### 2.3 Monthly Reward

- 월 1회 배치(기본 cron UTC 매월 1일 00:00) + 가입/초기 부여 경로
- `grade_reward_history` 회원·월 UNIQUE
- `GradeRewardGranted` 발행 (`rewardType=MONTHLY_FREE_TOKEN`, `tokenAmount`, `rewardMonth`)

### 2.4 Query

- 등급 목록, 내 등급/관리 화면, 현재 혜택 요약 조회

---

## 3. API 그룹

| 구분 | Prefix |
|------|--------|
| Grade Query | `/api/grade` — `GET /grades`, `/grades/me`, `/grades/me/management`, `/grades/me/benefits` |
| Deploy | `/api/grade/deploy-check` (또는 배포 스니펫 경로와 불일치 가능) |

쓰기/승급/보상은 **HTTP Command API 없음** → Kafka Consumer + Scheduler.

인증 헤더: `X-Member-UUID` (me 계열)

---

## 4. 외부 연동

| 방향 | 토픽(기본) | 내용 |
|------|------------|------|
| IN | `planwith.member.created` | 초기 등급 부여 |
| IN | `planwith.story.created` / `.deleted` | 스토리 메트릭 |
| IN | `planwith.follow.created` / `.removed` | 팔로워 메트릭 |
| IN | `planwith.like.created` / `.removed` | 좋아요 메트릭 |
| OUT | `planwith.grade.changed` | 등급 변경 (Member/Story/Membership 등 구독 예정) |
| OUT | `planwith.grade.reward-granted` | 월간 토큰 보상 (**Token 구독**) |

- HTTP 외부 클라이언트: 없음
- Redis: 등급 조회 캐시 (`grade:member`, TTL 10m)
- Token HTTP 호출: 없음 (이벤트만)

---

## 5. 비기능 / 품질

- Hexagonal (web/kafka/scheduler in · application · domain · jpa/kafka/redis/outbox out)
- Transactional Outbox + Relay (retry/backoff)
- 월간 보상 스케줄러, 기준 시드(`grade.criteria.seed-enabled`)
- 멱등: `processed_grade_event`, reward history UK
- 테스트: Domain/Application Unit, Kafka·Outbox·Persistence·Controller IT, Event Storming IT 등

---

## 6. 배포 설정 요약

| 항목 | 기본값 (`application.yml`) | 비고 |
|------|---------------------------|------|
| Port | `8083` | local `127.0.0.1` |
| Eureka | ON | local OFF |
| Kafka Consumer | ON | local OFF / infra compose에서 OFF 가능 |
| Outbox Relay | **OFF** | env `GRADE_OUTBOX_ENABLED=true`로 ON |
| Monthly Reward | ON | local에서 끌 수 있음 |
| Criteria Seed | ON | |
| JPA DDL | `update` | |
| Kafka bootstrap | `localhost:9092` | Docker는 env로 호스트명 |

서버 env: `planwith-infra/env/grade.env` (+ `common.env`)

---

## 7. 운영 주의사항

1. **Gateway 경로 불일치**: 스니펫 `/api/planwith-fo-grade/**` vs Controller `/api/grade/**` → 라우팅 맞출 것
2. **DB 이름**: `.env.example`/`grade_db` vs local yml `grade` → 환경별 통일
3. **Outbox 기본 OFF**면 `GradeRewardGranted`/`GradeChanged`가 Kafka로 안 나감 → Token FREE 미지급 원인
4. Grade는 잔액을 안 건드림. Token이 이벤트를 받아야 FREE 적립
5. 월중 승급해도 토큰은 즉시 증가하지 않음 → 다음 월간 지급 금액이 새 등급 기준
6. 가입 즉시 10토큰: Grade 초기 부여 시 해당 월 `GradeRewardGranted` 발행 경로(선택지 1) 확인
7. Eureka/앱 이름 `planwith-fo-grade`와 deploy-check `grade-service` 표기 혼재

---

## 8. 개발 완료 범위 (단계 요약)

```
01  Grade Criteria / Benefit Catalog Seed
02  GradeMember 초기 부여 (MemberCreated)
03  Metric 집계 (Story/Follow/Like)
04  Grade 평가·승급 + GradeChanged Outbox
05  월간 보상 Grant + GradeRewardGranted Outbox
06  Grade Query API (목록/내등급/혜택)
07  Redis 조회 캐시
08  Kafka Consumer / Outbox Relay
09  Domain·Integration·Storming 테스트
```

---

## 9. 검증 상태

- [x] Domain / Unit Test
- [x] Integration Test (Kafka / Outbox / Persistence / API)
- [x] Grade ↔ Token 이벤트 계약 (`GradeRewardGranted` payload)
- [ ] Gateway 경로·Swagger 정합
- [ ] 운영 Outbox/Kafka Consumer ON (인프라 준비 후)
- [ ] Token 측 Consumer ON과 E2E 월간 지급 검증

---

**RM 결론:** `planwith-fo-grade`는 등급 기준·메트릭 승급·월간 FREE 보상 이벤트·조회 API까지 기능 개발이 완료된 상태이며, 운영에서는 Outbox/Kafka ON, Gateway `/api/grade` 라우팅, Token 구독 활성화, DB 스키마명 통일을 맞추면 됩니다.
