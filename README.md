# microservice-member

QuietChatter의 회원 도메인 서비스. 네이버 OAuth 로그인, JWT 토큰 발급, 프로필 관리, 고객 문의 접수를 처리한다.

## 기술 스택

- 언어: Kotlin 1.9.25
- 프레임워크: Spring Boot 3.5.13
- 런타임: JDK 21 Virtual Threads 활성화
- 데이터베이스: PostgreSQL (JPA, Flyway), Redis (Refresh Token)
- 메시징: Spring Cloud Stream + Kafka (Redpanda)
- 포트: 8083 (k8s 배포 시 SERVER_PORT 환경변수로 주입, 로컬 기본값 8080)

## 환경변수 및 보안

모든 민감 정보는 k8s Secret(quietchatter-secrets)으로부터 환경 변수로 주입됩니다.

| 변수명 | 용도 | 비고 |
|---|---|---|
| SERVER_PORT | 서비스 포트 번호 | k8s: 8083 |
| DB_URL | PostgreSQL 접속 URL | |
| DB_USERNAME | PostgreSQL 사용자명 | |
| DB_PASSWORD | PostgreSQL 비밀번호 | |
| JWT_SECRET_KEY | JWT 서명 및 검증용 비밀키 | |
| INTERNAL_SECRET | 서비스 간 통신용 공유 비밀키 | |
| NAVER_CLIENT_ID | 네이버 OAuth 클라이언트 ID | |
| NAVER_CLIENT_SECRET | 네이버 OAuth 클라이언트 시크릿 | |
| KAFKA_BROKERS | Kafka 브로커 목록 | |
| SPRING_DATA_REDIS_HOST | Redis 호스트 주소 | |
| SPRING_DATA_REDIS_PORT | Redis 포트 번호 | |
| SPRING_PROFILES_ACTIVE | 활성 프로파일 | prod |

주의: Spring Cloud AWS Secrets Manager 의존성은 제거되었습니다. 모든 시크릿은 k8s 환경 변수를 통해 참조하십시오.

또한, 운영 환경(`application-prod.yml`)에서는 쿠키 도메인이 `quiet-chatter.com`으로 강제되며 `secure: true`, `same-site: Lax` 속성이 적용됩니다.

## 패키지 구조

헥사고날 아키텍처.

```
com.quietchatter.member/
  domain/          Member.kt, OauthProvider.kt, Role.kt, Status.kt
  application/
    in/            MemberCommandable.kt
    out/           MemberRepository.kt, TokenRepository.kt, OutboxEventPersistable.kt
  adaptor/
    in/web/        AuthController.kt, MeController.kt
    out/
      external/    NaverClient.kt
      messaging/   MemberIntegrationEvent.kt
      outbox/      OutboxEvent.kt, OutboxEventRepository.kt, OutboxPersistenceAdapter.kt, OutboxRelayService.kt

com.quietchatter.customer/
  adaptor/in/web/  CustomerMessageController.kt
```

## API 명세

### 인증 API (/api/auth, 인증 불필요)

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | /api/auth/login/naver | 네이버 OAuth 로그인. 기존 회원: ACCESS/REFRESH_TOKEN 쿠키 발급. 신규 회원: registerToken 반환 |
| POST | /api/auth/signup | 회원가입. Authorization: Bearer {registerToken} 헤더 필요 |
| POST | /api/auth/logout | 로그아웃. REFRESH_TOKEN 삭제 및 쿠키 만료 |
| POST | /api/auth/reactivate | 탈퇴 계정 재활성화. Authorization: Bearer {reactivationToken} 헤더 필요 |
| GET  | /api/auth/me | 현재 로그인 사용자 정보 조회. X-Member-Id 헤더 Optional (없을 시 isLoggedIn = false 반환) |
| GET  | /api/spec | OpenAPI 3 YAML 명세 제공 |

### 내 계정 API (/api/members/me, 인증 필요)

| 메서드 | 경로 | 설명 |
|---|---|---|
| PUT    | /api/members/me/profile | 닉네임 수정 |
| DELETE | /api/members/me | 회원 탈퇴 (DEACTIVATED 상태 전환 + 이벤트 발행) |

### 고객 문의 API (/api/support, 인증 불필요)

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | /api/support/messages | 고객 문의 접수 |

### 내부용 API (/internal/api, 외부 차단)

X-Internal-Secret 헤더 필수. 헤더 값이 INTERNAL_SECRET env var와 불일치 시 403 반환.

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | /internal/api/members/{memberId} | 회원 공개 정보(닉네임 등) 조회 |

## 도메인 모델

Member: id(UUID), nickname, providerId, provider(NAVER), role(REGULAR/MANAGER), status(ACTIVE/DEACTIVATED)

## 토큰 구조

| 토큰 | 저장 위치 | 만료 | 용도 |
|---|---|---|---|
| ACCESS_TOKEN | HttpOnly 쿠키 | 30분 | 일반 API 인증 |
| REFRESH_TOKEN | HttpOnly 쿠키 + Redis | 30일 | Access Token 재발급 |
| Register Token | 응답 바디 | 2시간 | 회원가입 전용 |
| Reactivation Token | 응답 바디 | 2시간 | 계정 재활성화 전용 |

## 에러 핸들링

RFC 7807 (Problem Details for HTTP APIs) 표준을 준수하며, @RestControllerAdvice를 통해 전역 예외 처리를 수행합니다.

## 이벤트

이벤트 포맷: CloudEvents 1.0 (specversion, id, source, type, time, subject, datacontenttype, data). 시각 필드는 RFC 3339(UTC) 형식.

발행 이벤트 (Kafka 토픽: member):

| type 필드 | 트리거 | data 필드 |
|---|---|---|
| com.quietchatter.member.MemberRegisteredEvent | 회원가입 | memberId, nickname |
| com.quietchatter.member.MemberDeactivatedEvent | 회원 탈퇴 | memberId |
| com.quietchatter.member.MemberProfileUpdatedEvent | 닉네임 수정 | memberId, nickname |

전송 패턴: Transactional Outbox. OutboxRelayService가 1초 간격으로 미처리 이벤트를 릴레이하고, 처리 완료된 이벤트는 7일 후 자동 삭제(매시간 정각 cleanup job).

## 닉네임 검증 규칙

2자 이상 10자 이하. 한글, 영문, 숫자만 허용. 특수문자 불허. 중복 허용.

## 로컬 실행

사전 요구 사항: Docker, JDK 21

```bash
./gradlew bootRun
```

로컬 실행 시 compose.yaml로 PostgreSQL, Redis가 자동 구동된다.
