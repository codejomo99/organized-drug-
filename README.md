# Organized Drug Profile 집계 및 모니터링

Bio Research AI에서 진행한 **Organized Drug Profile 집계 및 모니터링 과제** 요구사항을 바탕으로 작성된 프로젝트입니다. 각 모듈별 기능, 집계 규칙, 테스트 및 모니터링 방안을 모두 충족합니다.



## 목차

1. [과제 개요](#과제-개요)
2. [기능 요구사항 및 충족 여부](#기능-요구사항-및-충족-여부)
3. [집계 규칙](#집계-규칙)
4. [데이터 처리 규모 및 업데이트 전략](#데이터-처리-규모-및-업데이트-전략)
5. [실행 및 테스트](#실행-및-테스트)
6. [프로젝트 구조](#프로젝트-구조)

---

## 과제 개요

* 데이터 엔지니어링 팀에서 수집된 **중복 Drug Profile** 데이터를 하나의 레코드로 머지하는 집계 로직 구현
* **Web**(웹페이지)와 **Console**(콘솔) 환경에서 진행 상황 모니터링 기능 제공
* 중단 → 재시작이 가능한 프로세스 제어 및 실시간 로그/진행도 표시



## 기능 요구사항 및 충족 여부

| 모듈      | 요구사항                                                | 충족 여부 |
| ------- | --------------------------------------------------- |-------|
| Core    | • 집계 로직 구현 (`DrugProfile` → `OrganizedDrugProfile`) | O     |
|         | • 단위 테스트 코드 작성                                      | O     |
| Web     | • 버튼으로 시작/중단/재시작                                    | O     |
|         | • WebSocket/SSE 로 실시간 로그 및 진행도 표시                   | O     |
|         | • 서버 재시작 시에도 마지막 중단 지점부터 이어서 실행                     | O     |
|         | • 단위 및 통합(MockMvc) 테스트 작성                           | O     |
| Console | • `start`/`stop` 콘솔 명령 인터페이스 제공                     | X     |



## 집계 규칙

* 두 레코드가 **CompanyName**이 같고, **BrandName** 또는 **InnName** 또는 **CodeName** 중 하나라도 동일한 값이 있으면 같은 그룹으로 간주
* 필드는 `__` 구분자로 분리된 다중 값을 가질 수 있으며, 병합 시 중복을 제거하고 모두 유지

### 예시

```text
[A 데이터]
Company: Qpex Biopharma__Brii Biosciences
Brand  : Lucentis (US)__Susvimo
Inn    : Botulinum toxin A__Botulinum neurotoxin type A__abobotulinumtoxinA
Code   : ADC product candidate

[B 데이터]
Company: Brii Biosciences__FFF
Brand  : Lucentis (US)__Susvimo CC
Inn    : Botulinum toxin A
Code   : (없음)

[결과]
Company: Qpex Biopharma__Brii Biosciences__FFF
Brand  : Lucentis (US)__Susvimo__Susvimo CC
Inn    : Botulinum toxin A__Botulinum neurotoxin type A__abobotulinumtoxinA
Code   : ADC product candidate
```


## 데이터 처리 규모 및 업데이트 전략

* 테스트용 데이터는 약 10,000건이며, 실제 운영에서는 수십만 건 이상도 처리 가능하도록 설계
* **커서 기반 로딩**으로 메모리 부담 최소화
* **in-memory flag**(`AtomicBoolean`)로 즉시 중단/재시작 제어
* DB에는 `lastProcessedId` 만 기록하여 재시작 시 점프


## 실행 및 테스트

1. **Docker Compose 실행**

   ```bash
   docker-compose up --build -d
   ```
2. **웹 UI 접속**

    * `http://localhost:8080/index.html`
   





## 프로젝트 구조

```
backend/drug
├─ src
│   └─ main/java/com/side/drug
│       ├─ controller
│       ├─ service
│       ├─ repository
│       └─ model
├─ src/main/resources/static
│   ├─ css/styles.css
│   └─ js/app.js
├─ docker-compose.yml
├─ build.gradle
└─ settings.gradle
```


