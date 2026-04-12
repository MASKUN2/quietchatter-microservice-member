# microservice-member 구현 스펙

## 1. 서비스 역할

사용자 계정 관련 모든 기능을 담당합니다.
네이버 OAuth를 통한 로그인/회원가입, JWT 토큰 발급, 회원 정보 관리를 처리합니다.

## 2. 도메인 모델

### Member (회원)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | UUID | 기본 키 |
| nickname | String | 사용자 닉네임 |
| providerId | String | OAuth 제공사 ID (Naver ID) |
| provider | OauthProvider | OAuth 제공사 (NAVER) |
| role | Role | 권한 (REGULAR, MANAGER) |
| status | Status | 계정 상태 (ACTIVE, DEACTIVATED) |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

## 3. API 명세

### 3.1 인증 API (인증 불필요)

#### POST /v1/auth/login/naver
네이버 OAuth 코드를 받아 로그인 또는 신규 사용자 감지.

요청 바디:
```json
{
  "code": "naver_auth_code",
  "state": "random_state_string"
}
```

응답 (기존 회원):
```json
{
  "registered": true
}
```

응답 (신규 회원):
```json
{
  "registered": false,
  "registerToken": "short_lived_jwt_for_signup",
  "tempNickname": "랜덤닉네임123"
}
```

* 기존 회원: ACCESS_TOKEN과 REFRESH_TOKEN을 쿠키에 설정하고 응답.
* 신규 회원: 회원가입용 단기 토큰(registerToken)을 응답 바디에 포함.

#### POST /v1/auth/signup
신규 회원 가입 처리.

요청 헤더: `Authorization: Bearer {registerToken}`

요청 바디:
```json
{
  "nickname": "원하는닉네임"
}
```

응답: 201 Created (ACCESS_TOKEN, REFRESH_TOKEN 쿠키 설정)

#### POST /v1/auth/logout
로그아웃. REFRESH_TOKEN을 Redis에서 삭제하고 쿠키를 만료시킴.

응답: 204 No Content

#### POST /v1/auth/reactivate
탈퇴한 계정 재활성화.

요청 헤더: `Authorization: Bearer {reactivationToken}`

응답: 200 OK (ACCESS_TOKEN, REFRESH_TOKEN 쿠키 설정)

#### GET /v1/auth/me
현재 로그인한 사용자 기본 정보 조회.

요청 헤더: `X-Member-Id: {memberId}` (Gateway가 주입)

응답:
```json
{
  "isLoggedIn": true,
  "id": "uuid",
  "nickname": "닉네임",
  "role": "REGULAR"
}
```

### 3.2 내 계정 API (인증 필요: X-Member-Id 헤더)

#### PUT /v1/me/profile
내 닉네임 수정.

요청 바디:
```json
{
  "nickname": "새닉네임"
}
```

응답: 204 No Content

#### DELETE /v1/me
회원 탈퇴.

처리 순서:
1. Member 상태를 DEACTIVATED로 변경
2. MemberDeactivatedEvent 발행 (Kafka/Outbox)
3. REFRESH_TOKEN 삭제
4. 쿠키 만료 처리

응답: 204 No Content

## 4. 토큰 종류 및 만료 시간

| 토큰 | 저장 위치 | 만료 시간 | 용도 |
|---|---|---|---|
| ACCESS_TOKEN | 쿠키 (HttpOnly) | 30분 | 일반 API 인증 |
| REFRESH_TOKEN | 쿠키 (HttpOnly) + Redis | 30일 | Access Token 재발급 |
| Register Token | 응답 바디 | 2시간 | 회원가입 전용 |
| Reactivation Token | 응답 바디 | 2시간 | 계정 재활성화 전용 |

## 5. 닉네임 검증 규칙

* 2자 이상 10자 이하
* 한글, 영문, 숫자만 허용
* 특수문자 불허
* 중복 허용 (고유하지 않아도 됨)

## 6. 랜덤 닉네임 생성

신규 회원 감지 시 임시 닉네임을 생성합니다.
레거시의 `member/adaptor/RandomNickNameSupplierImpl.java`를 참고하여 구현하십시오.

## 7. 서비스 간 통신

* 이벤트 발행: `MemberRegisteredEvent`, `MemberDeactivatedEvent`
* 통신 방식: Kafka (Redpanda) + Transactional Outbox Pattern
* microservice-talk 등 타 서비스에서 해당 이벤트를 구독하여 후속 처리(북톡 숨김 등)를 수행합니다.

## 8. 구현 우선순위

1. Member 도메인 및 JPA 설정
2. Naver OAuth 연동 및 로그인/가입 API
3. JWT 토큰 발급 및 쿠키 처리
4. 프로필 수정 및 탈퇴 API
5. 서비스 간 통신
