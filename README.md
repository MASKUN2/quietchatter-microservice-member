# microservice-member

QuietChatter의 회원 도메인 서비스. 네이버 OAuth 로그인, JWT 토큰 발급, 프로필 관리, 고객 문의 접수를 처리한다.

## 기술 스택

- 언어: Kotlin 1.9.25
- 프레임워크: Spring Boot 3.5.13
- 런타임: JDK 21 Virtual Threads 활성화
- 데이터베이스: PostgreSQL (JPA, Flyway), Redis (Refresh Token)
- 메시징: Spring Cloud Stream + Kafka (Redpanda)
- 포트: 8083 (k8s 배포 시 SERVER_PORT 환경변수로 주입, 로컬 기본값 8080)
- 시크릿: k8s Secret 오브젝트 경유 env var 주입. Spring Cloud AWS Secrets Manager 직접 조회 방식 미사용

## 패키지 구조

헥사고날 아키텍처.

```
com.quietchatter.member/
  domain/          Member.kt, OauthProvider.kt, Role.kt, Status.kt
  application/
    in/            MemberCommandable.kt, AuthMemberService.kt
    out/           MemberRepository.kt, TokenRepository.kt
  adaptor/
    in/web/        AuthController.kt, MeController.kt
    out/           MemberJpaRepository.kt, TokenRedisRepository.kt, NaverOAuthClient.kt

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
| GET  | /api/auth/me | 현재 로그인 사용자 정보 조회. X-Member-Id 헤더 필요 |

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

- 발행: MemberRegisteredEvent, MemberDeactivatedEvent, MemberProfileUpdatedEvent (Kafka 토픽: member)
- 전송 패턴: Transactional Outbox

## 닉네임 검증 규칙

2자 이상 10자 이하. 한글, 영문, 숫자만 허용. 특수문자 불허. 중복 허용.

## 로컬 실행

사전 요구 사항: Docker, JDK 21

```bash
./gradlew bootRun
```

로컬 실행 시 compose.yaml로 PostgreSQL, Redis가 자동 구동된다.
