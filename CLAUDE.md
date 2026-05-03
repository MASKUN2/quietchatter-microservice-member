# CLAUDE.md - microservice-member

작업 전 README.md를 읽으십시오. 서비스 개요, 기술 스택, API 명세, 도메인 모델, 토큰 구조는 README.md에 있습니다.

루트 프로젝트의 CLAUDE.md에 정의된 공통 원칙도 확인하십시오.

## 작업 지침

### A. 코드 포팅 규칙

- 레거시의 member/, security/ 패키지를 참고하여 idiomatic Kotlin으로 재작성하십시오.
- !! 연산자 사용 최소화. 안전 연산자(?., ?:)를 적극 활용하십시오.
- 새로운 코드 작성 또는 수정 시 단위 테스트를 함께 작성하고 통과를 확인하십시오.
- 라이브러리 추가 시 Kotlin 친화적 의존성(예: mockito-kotlin)을 사용하십시오.

### B. 토큰 처리 규칙

- JWT Access Token과 Refresh Token 발급은 이 서비스가 담당한다.
- Refresh Token은 Redis에 저장하고 tokenId를 키로 사용한다.
- 이 서비스 내에서 일반 API 요청의 JWT를 검증하지 마십시오. X-Member-Id 헤더를 신뢰하십시오.
- Gateway 계약: 토큰이 있으면 X-Member-Id 헤더가 전달되고, 없으면 헤더가 아예 전송되지 않는다. 빈 문자열은 오지 않는다.
- 인증 필수 엔드포인트에서 X-Member-Id 헤더가 없으면 MissingRequestHeaderException이 발생하며, GlobalExceptionHandler가 이를 401로 처리한다.
- 레거시의 AuthTokenService.java를 참고하여 쿠키 설정 로직을 구현하십시오.

### C. OAuth 규칙

- Naver OAuth 연동은 adaptor/out에 구현하고 포트 인터페이스를 통해 application 레이어와 통신하십시오.
- 레거시의 AuthMemberServiceImpl.java를 참고하십시오.

### D. 메시징 규칙

- 모든 이벤트 발행은 Transactional Outbox 패턴을 따른다.
- 이벤트 포맷: CloudEvents 1.0. MemberIntegrationEvent 클래스를 사용하며 필드는 specversion, id, source, type, time, subject, datacontenttype, data를 포함한다.
- type 필드 명명 규칙: com.quietchatter.member.{EventName} (예: com.quietchatter.member.MemberDeactivatedEvent).
- time 필드: LocalDateTime.atOffset(ZoneOffset.UTC).toString()으로 RFC 3339 형식 직렬화.
- MemberService는 OutboxEventPersistable 포트를 통해 이벤트를 저장한다. OutboxEventRepository를 직접 주입하지 마십시오.

### E. 데이터베이스 변경

- 스키마 변경 시 src/main/resources/db/migration에 Flyway 스크립트를 작성하십시오.
- IF NOT EXISTS 구문과 기본값 설정으로 기존 데이터를 보호하십시오.
