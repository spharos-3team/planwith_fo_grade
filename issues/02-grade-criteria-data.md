## 작업 내용

등급 기준 데이터(등급표, 승급 조건, 혜택 정의)를 DB에 적재하고 조회할 수 있도록 구성한다.

## 요구사항

- grade-service 전용 DB만 사용한다
- 등급 기준 데이터는 Command/Query 양쪽에서 참조 가능해야 한다
- 기존 ERD 테이블/컬럼명을 따른다

## 작업 상세

- [ ] 등급 기준 Entity / Repository Adapter 구현
- [ ] 초기 등급 기준 데이터 시드 또는 마이그레이션 전략 수립
- [ ] 등급 기준 조회 Port Out 정의
- [ ] Test 작성

## 참고사항

- JPA `ddl-auto=update` 환경. 테이블은 JPA Entity로 생성
- 등급 코드, 승급 Metric 임계값, 혜택 정보 포함
