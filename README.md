# AutoTrade - 암호화폐 김치프리미엄 자동매매 시스템

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 목차

- [개요](#개요)
- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [설치 및 설정](#설치-및-설정)
- [API 문서](#api-문서)
- [사용법](#사용법)
- [배치 스케줄](#배치-스케줄)
- [보안 및 리스크 관리](#보안-및-리스크-관리)
- [라이센스](#라이센스)

## 🚀 개요

AutoTrade는 **업비트와 바이낸스 간의 김치프리미엄을 활용한 자동매매 시스템**입니다. 헤징 전략을 통해 업비트 현물 매수와 바이낸스 선물 숏 포지션을 동시에 실행하여 안정적인 수익을 추구합니다.

### 💡 핵심 전략

- **헤징 거래**: 업비트 현물 매수 + 바이낸스 선물 숏
- **김치프리미엄 활용**: 국내외 거래소 간 가격 차이 수익화
- **리스크 최소화**: 방향성 리스크 제거, 김프 수렴 수익 실현

## ✨ 핵심 기능

### 🔄 자동매매 시스템
- **실시간 김프 모니터링**: 10분 주기 김치프리미엄 데이터 수집
- **동시 주문 실행**: CompletableFuture를 활용한 지연 최소화
- **자동 포지션 관리**: 진입/정리 자동화
- **레버리지 지원**: 바이낸스 선물 레버리지 활용

### 📊 데이터 분석
- **실시간 김프 계산**: 정확한 환율 기반 김프율 산출
- **수익성 분석**: 임계값 기반 거래 기회 탐지
- **통계 분석**: 변동성 및 수익성 지표 제공
- **히스토리 관리**: 모든 김프 데이터 저장 및 조회

### 🛡️ 리스크 관리
- **잔고 검증**: 거래 전 충분한 자금 확인
- **일일 한도**: 거래 횟수 및 손실 제한
- **에러 복구**: 실패 거래 자동 보상 시스템
- **Circuit Breaker**: 외부 API 장애 대응

### 🔐 보안 시스템
- **JWT 인증**: 무상태 토큰 기반 사용자 인증
- **API 키 암호화**: 거래소 키 안전 저장
- **Role 기반 접근 제어**: 권한별 API 접근 관리

## 🏗️ 시스템 아키텍처



## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.4.5** - 메인 프레임워크
- **Spring Security** - 인증 및 보안
- **Spring Data JPA** - 데이터 액세스 레이어
- **Spring Batch** - 배치 처리 (자동매매)
- **Spring WebFlux** - 반응형 HTTP 클라이언트

### Database & ORM
- **MySQL 8.0** - 메인 데이터베이스
- **Hibernate** - ORM 구현체
- **HikariCP** - 커넥션 풀링

### External Integration
- **Resilience4j** - Circuit Breaker, Retry, Timeout
- **Spring AI MCP Server** - AI 도구 통합
- **JWT (jjwt)** - JSON Web Token 구현
- **WebClient** - HTTP 클라이언트

### Development & Documentation
- **Java 17** - 언어 버전
- **Gradle** - 빌드 도구
- **Swagger/OpenAPI 3** - API 문서화
- **Lombok** - 코드 간소화

### Monitoring & Operations
- **Spring Boot Actuator** - 모니터링 엔드포인트
- **Logback** - 로깅 프레임워크

## ⚙️ 설치 및 설정

### 1. 사전 요구사항

- **Java 17** 이상
- **MySQL 8.0** 이상
- **Gradle 7.x** 이상

### 2. 데이터베이스 설정

```sql
-- MySQL 데이터베이스 생성
CREATE DATABASE autotrade;
CREATE USER 'autotrade_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON autotrade.* TO 'autotrade_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다:

```env
# Database Configuration
DB_URL=127.0.0.1:3306/autotrade
DB_USER=autotrade_user
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET_KEY=mySecretKeyForJwtTokenServiceShouldBeAtLeast256BitsLongToWorkProperly
JWT_EXPIRATION_MS=86400000

# Upbit API Configuration
UPBIT_URL=https://api.upbit.com/
UPBIT_ACCESS_KEY=your_upbit_access_key
UPBIT_SECRET_KEY=your_upbit_secret_key

# Binance API Configuration
BINANCE_URL=https://api1.binance.com
BINANCE_FUTURES_URL=https://fapi.binance.com
BINANCE_API_KEY=your_binance_api_key
BINANCE_SECRET_KEY=your_binance_secret_key

# Exchange Rate API
EXCHANGE_API_URL=https://ecos.bok.or.kr/api
EXCHANGE_API_KEY=your_exchange_api_key
```

### 4. 애플리케이션 실행

```bash
# 환경 변수와 함께 실행
export $(cat .env | xargs) && ./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
export $(cat .env | xargs) && java -jar build/libs/AutoTrade-0.0.1-SNAPSHOT.jar
```

### 5. 초기 데이터 설정

애플리케이션 실행 후 다음 코인 페어들을 추가하세요:

```bash
# BTC 페어 추가
curl -X POST http://localhost:8080/api/coin-pairs \
  -H "Content-Type: application/json" \
  -d '{
    "upbitSymbol": "KRW-BTC",
    "binanceSymbol": "BTCUSDT", 
    "coinName": "Bitcoin",
    "isActive": true,
    "batchEnabled": true
  }'

# ETH 페어 추가  
curl -X POST http://localhost:8080/api/coin-pairs \
  -H "Content-Type: application/json" \
  -d '{
    "upbitSymbol": "KRW-ETH",
    "binanceSymbol": "ETHUSDT",
    "coinName": "Ethereum", 
    "isActive": true,
    "batchEnabled": true
  }'
```

## 📚 API 문서

### Swagger UI 접근
- **URL**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### 주요 API 엔드포인트

#### 🔐 인증 API (`/api/auth`)
```bash
# 회원가입
POST /api/auth/signup

# 로그인 
POST /api/auth/login

# 이메일 중복 확인
GET /api/auth/check-email?email=user@example.com
```

#### 📊 김프 데이터 API (`/api/kimp`) - 공개 접근
```bash
# 최신 김프 데이터 조회
GET /api/kimp/latest

# 특정 코인 김프 조회
GET /api/kimp/latest/KRW-BTC/BTCUSDT

# 김프 히스토리 조회
GET /api/kimp/history/KRW-BTC/BTCUSDT?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00

# 수익성 김프 조회
GET /api/kimp/profitable?upbitSymbol=KRW-BTC&binanceSymbol=BTCUSDT&minKimp=2.5

# 김프 통계
GET /api/kimp/statistics/KRW-BTC/BTCUSDT?hours=24

# 수동 김프 수집
POST /api/kimp/collect-now
```

#### 🪙 코인 페어 관리 API (`/api/coin-pairs`) - 공개 접근
```bash
# 모든 코인 페어 조회
GET /api/coin-pairs

# 활성 코인 페어 조회
GET /api/coin-pairs/active

# 코인 페어 활성화 토글
PUT /api/coin-pairs/1/toggle-active

# 배치 수집 활성화 토글
PUT /api/coin-pairs/1/toggle-batch
```

#### ⚙️ 자동매매 설정 API (`/api/autotrade`) - 인증 필요
```bash
# 자동매매 설정 조회 (JWT 토큰 필요)
GET /api/autotrade
Authorization: Bearer YOUR_JWT_TOKEN

# 자동매매 설정 생성
POST /api/autotrade
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
{
  "coinPair": "KRW-BTC",
  "minKimpThreshold": 2.5,
  "maxKimpThreshold": 5.0,
  "leverage": 3.0,
  "seedMoney": 1000000,
  "isActive": true
}

# 자동매매 활성화 토글
PUT /api/autotrade/1/toggle
Authorization: Bearer YOUR_JWT_TOKEN
```

#### 💱 거래 API (`/api/trade`) - 인증 필요
```bash
# 헤지 거래 실행
POST /api/trade/order
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
{
  "upbitSymbol": "KRW-BTC",
  "binanceSymbol": "BTCUSDT",
  "volume": 0.001,
  "price": 71000000,
  "leverage": 3.0
}

# 포지션 정리
POST /api/trade/close
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🎯 사용법

### 1. 시스템 초기 설정

#### 사용자 등록 및 로그인
```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "trader1",
    "email": "trader@example.com", 
    "password": "securepassword123",
    "name": "김거래"
  }'

# 2. 로그인하여 JWT 토큰 획득
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "trader@example.com",
    "password": "securepassword123"
  }'

# 응답에서 token 값을 저장
# {"code":"SUCCESS","data":{"token":"eyJhbGciOiJIUzI1NiJ9..."}}
```

#### 코인 페어 활성화
```bash
# BTC, ETH, XRP 페어 활성화
curl -X PUT http://localhost:8080/api/coin-pairs/1/toggle-active  # BTC
curl -X PUT http://localhost:8080/api/coin-pairs/2/toggle-active  # ETH  
curl -X PUT http://localhost:8080/api/coin-pairs/3/toggle-active  # XRP
```

### 2. 자동매매 설정

```bash
# JWT 토큰을 사용하여 자동매매 설정 생성
export JWT_TOKEN="your_jwt_token_here"

curl -X POST http://localhost:8080/api/autotrade \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "upbitSymbol": "KRW-BTC",
    "binanceSymbol": "BTCUSDT", 
    "minKimpThreshold": 2.5,
    "maxKimpThreshold": 8.0,
    "leverage": 3.0,
    "seedMoney": 1000000,
    "maxDailyTrades": 10,
    "maxDailyLoss": 100000,
    "isActive": true
  }'
```

### 3. 모니터링

#### 실시간 김프 모니터링
```bash
# 최신 김프 데이터 확인
curl http://localhost:8080/api/kimp/latest

# 특정 코인의 김프 통계
curl "http://localhost:8080/api/kimp/statistics/KRW-BTC/BTCUSDT?hours=24"

# 수익성 높은 김프 조회
curl "http://localhost:8080/api/kimp/profitable?upbitSymbol=KRW-BTC&binanceSymbol=BTCUSDT&minKimp=3.0&hours=1"
```

#### 자동매매 상태 확인
```bash
# 활성화된 자동매매 설정 조회
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/autotrade/active

# 거래 히스토리 확인 (Swagger UI 사용 권장)
```

### 4. 수동 거래 (테스트용)

```bash
# 수동으로 헤지 거래 실행
curl -X POST http://localhost:8080/api/trade/order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "upbitSymbol": "KRW-BTC",
    "binanceSymbol": "BTCUSDT",
    "volume": 0.001,
    "price": 71000000,
    "leverage": 3.0
  }'
```

## ⏰ 배치 스케줄

### 자동 실행 배치들

| 배치명 | 실행 주기 | 기능 |
|--------|-----------|------|
| **김프 데이터 수집** | 10분마다 | 활성 코인 페어의 김프 데이터 수집
| **자동매매 실행** | 10분마다 | 김프 조건 충족시 헤지 거래 실행
| **보상 거래 처리** | 5분마다 | 실패한 거래의 보상 처리 재시도



## 🛡️ 보안 및 리스크 관리

### 인증 및 보안

#### JWT 토큰 기반 인증
- **토큰 유효기간**: 24시간
- **갱신 방식**: 재로그인 필요
- **암호화**: HMAC-SHA256

#### API 키 보안
- **암호화 저장**: 거래소 API 키 암호화하여 데이터베이스 저장
- **환경 변수**: 민감한 설정값은 환경 변수로 관리
- **접근 제어**: Role 기반 API 접근 제어

### 리스크 관리 시스템


#### 에러 복구 시스템
1. **즉시 보상**: 한쪽 거래만 성공시 즉시 반대 거래 시도
2. **큐 저장**: 즉시 보상 실패시 보상 큐에 저장
3. **배치 재처리**: 5분마다 실패 거래 재시도 (최대 3회)

#### Resilience4j 패턴
```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalApi:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      externalApi:
        maxAttempts: 3
        waitDuration: 2s
```

### 모니터링

#### Spring Boot Actuator
- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Info**: `GET /actuator/info`

#### 로깅 전략
- **거래 로그**: 모든 거래 시도 및 결과 기록
- **에러 로그**: 외부 API 호출 실패 상세 기록
- **성능 로그**: 배치 실행 시간 모니터링

## 🤝 기여

이 프로젝트에 기여하고 싶으시다면:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

---

## ⚠️ 면책 조항

이 소프트웨어는 교육 및 연구 목적으로 제공됩니다. 실제 거래에 사용할 경우 발생하는 모든 손실에 대해 개발자는 책임지지 않습니다. 

투자에는 항상 리스크가 따르며, 투자 전 충분한 검토와 테스트를 권장합니다.

---

## 📞 문의

질문이나 제안사항이 있으시면 다음으로 연락주세요:

- 이메일: hanyj0104@naver.com
- GitHub Issues: [프로젝트 Issues 페이지](https://github.com/ojin0104/AutoTrade/issues)

---

**Happy Trading! 🚀**