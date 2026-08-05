# 미션 목록 조회 N+1 측정 가이드

`GET /api/v1/missions`의 N+1 개선 전후를 같은 조건으로 비교하기 위한 측정 가이드입니다.

## 1. 목표

- 기능 회귀 없이 미션 목록 조회 성능을 비교한다.
- 개선 전후 SQL 수 차이를 확인한다.
- 단건 호출 시간과 간단 부하 상황에서의 응답 시간 차이를 확인한다.

## 2. 측정 항목

### 기능 테스트

- 기존 테스트:
  - `com.zerost.api.mission.application.MissionQueryServiceTest`
- 목적:
  - 응답 구조와 today status 계산이 바뀌지 않았는지 확인

### SQL 수 + 단건 시간

- 측정 테스트:
  - `com.zerost.api.mission.application.MissionQueryServiceMeasurementTest`
- 출력 예시:

```text
MISSION_QUERY_BASELINE sql=7 firstRunMs=165.00 avgMs=28.40 minMs=18.32 maxMs=41.77
```

- 확인 항목:
  - `sql`
  - `firstRunMs`
  - `avgMs`
  - `minMs`
  - `maxMs`

### 부하테스트

- 스크립트:
  - `scripts/loadtest/mission-list.k6.js`
- 목적:
  - 동시 요청이 들어왔을 때 avg / p95 / 에러율 비교

## 3. 로컬 측정 순서

### 3-1. 기능 테스트

```bash
./gradlew test --tests 'com.zerost.api.mission.application.MissionQueryServiceTest'
```

### 3-2. SQL 수 + 단건 시간 측정

```bash
./gradlew test --tests 'com.zerost.api.mission.application.MissionQueryServiceMeasurementTest'
```

테스트 결과 파일에서 측정 로그만 다시 확인하려면:

```bash
grep -RIn "MISSION_QUERY_BASELINE" build/test-results build/reports
```

## 4. 부하테스트 시나리오

같은 access token으로 `GET /api/v1/missions`만 반복 호출합니다.

### 시나리오 A. 단일 사용자 반복 조회

- 목적:
  - 순수 조회 성능 baseline 확인
- 설정:
  - `1 VU`
  - `30초`

### 시나리오 B. 소규모 동시 사용자

- 목적:
  - N+1로 인한 반복 쿼리 비용이 동시 요청에서 어떻게 보이는지 확인
- 설정:
  - `10 VU`
  - `1분`

### 시나리오 C. 중간 부하

- 목적:
  - avg 뿐 아니라 p95 차이를 보기 위한 구간
- 설정:
  - `30 VU`
  - `1분`

### 시나리오 D. 짧은 스파이크

- 목적:
  - 순간 부하에서 응답 시간 튐 여부 확인
- 설정:
  - `50 VU`
  - `30초`

## 5. k6 실행 예시

### dev 환경 예시

```bash
k6 run \
  -e BASE_URL=https://dev-api.zero-st.com \
  -e ACCESS_TOKEN='여기에_액세스_토큰' \
  scripts/loadtest/mission-list.k6.js
```

### 권장 실행 횟수

- 워밍업:
  - 전체 시나리오 1회
  - 결과 기록/캡처 제외
- 실제 측정:
  - 같은 조건으로 3회 반복 실행
  - `avg`, `p95`, `http_req_failed`를 각각 기록
- 결과 정리:
  - 3회 결과의 평균값 또는 중간값 사용

포트폴리오 용도로는 아래 3개 시나리오만 우선 비교해도 충분합니다.

- `1 VU`
- `10 VU`
- `30 VU`

`50 VU`는 스파이크 참고 자료로 선택적으로 사용하면 됩니다.

### 시나리오별 복붙용 실행 명령어

아래 명령어에서 `ACCESS_TOKEN`만 바꿔서 사용하면 됩니다.

### 반복 실행 방식

같은 시나리오를 여러 번 측정할 때는 쉘 루프로 한 번에 실행하는 방식을 권장합니다.

- 워밍업:
  - 1회 실행
- 실제 측정:
  - `for i in 1 2 3` 형태로 3회 실행
- 스파이크:
  - 필요하면 2회만 실행

#### 0. 워밍업 1회

```bash
k6 run \
  -e BASE_URL=https://dev-api.zero-st.com \
  -e ACCESS_TOKEN='여기에_액세스_토큰' \
  scripts/loadtest/mission-list.k6.js
```

#### 1. 단일 사용자 반복 조회 3회

```bash
for i in 1 2 3; do
  echo "== single-user run $i =="
  k6 run \
    -e BASE_URL=https://dev-api.zero-st.com \
    -e ACCESS_TOKEN='여기에_액세스_토큰' \
    -e SMALL_VUS=0 \
    -e MEDIUM_VUS=0 \
    -e SPIKE_VUS=0 \
    scripts/loadtest/mission-list.k6.js
done
```

#### 2. 10 VU 소규모 동시 요청 3회

```bash
for i in 1 2 3; do
  echo "== 10-vu run $i =="
  k6 run \
    -e BASE_URL=https://dev-api.zero-st.com \
    -e ACCESS_TOKEN='여기에_액세스_토큰' \
    -e SMOKE_VUS=0 \
    -e MEDIUM_VUS=0 \
    -e SPIKE_VUS=0 \
    scripts/loadtest/mission-list.k6.js
done
```

#### 3. 30 VU 중간 부하 3회

```bash
for i in 1 2 3; do
  echo "== 30-vu run $i =="
  k6 run \
    -e BASE_URL=https://dev-api.zero-st.com \
    -e ACCESS_TOKEN='여기에_액세스_토큰' \
    -e SMOKE_VUS=0 \
    -e SMALL_VUS=0 \
    -e SPIKE_VUS=0 \
    scripts/loadtest/mission-list.k6.js
done
```

#### 4. 50 VU 스파이크 2회

```bash
for i in 1 2; do
  echo "== 50-vu spike run $i =="
  k6 run \
    -e BASE_URL=https://dev-api.zero-st.com \
    -e ACCESS_TOKEN='여기에_액세스_토큰' \
    -e SMOKE_VUS=0 \
    -e SMALL_VUS=0 \
    -e MEDIUM_VUS=0 \
    scripts/loadtest/mission-list.k6.js
done
```

### 시나리오 수치 조정 예시

```bash
k6 run \
  -e BASE_URL=https://dev-api.zero-st.com \
  -e ACCESS_TOKEN='여기에_액세스_토큰' \
  -e SMALL_VUS=20 \
  -e MEDIUM_VUS=40 \
  -e SPIKE_VUS=60 \
  scripts/loadtest/mission-list.k6.js
```

## 6. 비교할 때 남기면 좋은 결과

개선 전과 개선 후 각각 아래를 남기면 됩니다.

- 기능 테스트 통과 캡처
- `MISSION_QUERY_BASELINE ...` 로그 캡처
- k6 요약 결과 캡처
  - `http_req_duration avg`
  - `http_req_duration p(95)`
  - `http_req_failed`
  - 시나리오별 처리량
- 같은 시나리오를 여러 번 돌렸다면:
  - 각 회차 결과
  - 평균값 또는 중간값 정리 표

## 7. 포트폴리오 정리 예시

- 개선 전:
  - 미션 목록 조회 1회에 SQL이 몇 개 실행됐는지
  - 단건 평균 시간이 어느 정도였는지
- 개선 후:
  - SQL 수가 얼마나 줄었는지
  - avg / p95가 얼마나 개선됐는지
- 해석:
  - 미션별 today completion 조회가 반복되던 구조를 배치 조회로 바꾸면서 조회 수를 줄였고, 동시 요청 상황에서 응답 시간 분포도 함께 안정화됐다
