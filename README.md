# Organized Drug Profile 집계 및 모니터링

Bio Research AI에서 진행한 **Organized Drug Profile 집계 및 모니터링 과제** 요구사항을 바탕으로 작성된 프로젝트입니다. 각 모듈별 기능, 집계 규칙, 테스트 및 모니터링 방안을 모두 충족합니다.



## 목차

1. [과제 개요](#과제-개요)
2. [모듈 구성](#모듈-구성)
3. [기능 요구사항 및 충족 여부](#기능-요구사항-및-충족-여부)
4. [집계 규칙](#집계-규칙)
5. [데이터 처리 규모 및 업데이트 전략](#데이터-처리-규모-및-업데이트-전략)
6. [실행 및 테스트](#실행-및-테스트)
7. [Docker 환경](#docker-환경)
8. [프로젝트 구조](#프로젝트-구조)



## 과제 개요

* 데이터 엔지니어링 팀에서 수집된 **중복 Drug Profile** 데이터를 하나의 레코드로 머지하는 집계 로직 구현
* **Web**(웹페이지)와 **Console**(콘솔) 환경에서 진행 상황 모니터링 기능 제공
* 중단 → 재시작이 가능한 프로세스 제어 및 실시간 로그/진행도 표시



## 모듈 구성

1. **Core**
   * 실제 집계 로직을 구현하는 모듈
   * 단위 테스트 작성
2. **Web**
   * 웹 UI에서 버튼으로 집계 시작/중단/재시작
   * WebSocket 기반 실시간 로그 및 진행도 표시
   * 단위 테스트 및 통합 테스트 작성



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

---

## 데이터 처리 규모 및 업데이트 전략

* 테스트용 데이터는 약 10,000건이며, 실제 운영에서는 수십만 건 이상도 처리 가능하도록 설계
* **커서 기반 로딩**(ID 기반 페이징)으로 메모리 부담 최소화
* **in-memory flag**(`AtomicBoolean`)로 즉시 중단/재시작 제어
* DB에는 `lastProcessedId` 만 기록하여 재시작 시 점프


## 환경 구성

* 원래 과제 요구사항에는 MariaDB를 사용하도록 명시되어 있었으나, 개발 환경에서 SHA256 인증 에러가 지속 발생하여 PostgreSQL로 대체 구성하였습니다.
* Docker Compose 파일에서 아래와 같이 구성합니다:

```yaml
services:
  db:
    image: postgres:13
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 1234
      POSTGRES_DB: drug_profile
    ports:
      - "5432:5432"

  app:
    # ...
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/drug_profile
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: 1234
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_JPA_SHOW_SQL: "true"
      SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.PostgreSQLDialect
```

## 실행 및 테스트

1. **Docker Compose 실행**

   ```bash
   docker-compose up --build -d
   ```
2. **웹 UI 접속**

   * `http://localhost:8080/index.html`

   
## Docker 환경

* **멀티스테이지 Dockerfile**

   1. `gradle bootJar` 로 JAR 생성
   2. `openjdk:17-jdk-slim` 기반 런타임 이미지  
  

* **docker-compose.yml**

   * PostgreSQL 서비스
   * Spring Boot 앱
   * `docker-compose up --build -d` 로 즉시 실행

---

## 프로젝트 구조

```
backend/drug
├─ src/main/java
│   └─ com/side/drug
│       ├─ controller
│       ├─ service
│       ├─ repository
│       └─ model
├─ Dockerfile
├─ src/main/resources/static
│   ├─ css/styles.css
│   └─ js/app.js
├─ docker-compose.yml
├─ build.gradle
└─ settings.gradle
```


