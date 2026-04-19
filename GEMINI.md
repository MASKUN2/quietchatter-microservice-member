# AI Agent Guide - microservice-member

이 문서는 AI 에이전트가 microservice-member 프로젝트를 이해하고 개발을 돕기 위한 지침입니다.

## 1. 서비스 개요

* 역할: 회원 계정 관리 (OAuth 로그인, 회원가입, 프로필 수정, 탈퇴) 및 JWT 토큰 발급
* 담당 레거시 패키지: member, security
* 포트: 8080

## 2. 기술 스택

* 언어: Kotlin 1.9.x
* 프레임워크: Spring Boot 3.5.13
* 데이터베이스: PostgreSQL (JPA), Redis (Refresh Token 저장)
* 인증: jjwt (JWT 발급 전용, 검증은 Gateway가 담당)
* 의존성: spring-boot-starter-web, data-jpa, data-redis, consul-discovery, consul-config

## 3. 아키텍처

헥사고날 아키텍처(Ports and Adapters)를 사용합니다.

```
adaptor/in  (Web Controller: @RestController)
    |
application (Use Case Service: Port Interface + Impl)
    |
adaptor/out (JPA Repository, Redis Client, External OAuth Client)
    |
domain      (Entity, Value Object: 순수 비즈니스 로직)
```

패키지 구조 예시:
```
com.quietchatter.member/
  domain/          Member.kt, OauthProvider.kt, Role.kt, Status.kt
  application/
    in/            MemberCommandable.kt, AuthMemberService.kt
    out/           MemberRepository.kt, TokenRepository.kt
  adaptor/
    in/            AuthController.kt, MeController.kt
    out/           MemberJpaRepository.kt, TokenRedisRepository.kt, NaverOAuthClient.kt
```

## 4. 에이전트 작업 지침

모든 작업 시작 전 및 작업 중에 superpowers 스킬 목록을 항상 확인하고 상황에 맞는 스킬을 활성화하여 사용하십시오.

### A. 공통 원칙

* 모든 서비스는 헥사고날 아키텍처를 따르며, 어댑터 패키지 명칭은 adaptor로 통일합니다.

### B. 코드 포팅 규칙

* 레거시 Java 코드를 idiomatic Kotlin 코드로 변환하십시오.
* Null safety를 적극 활용하십시오. !! 연산자 사용을 최소화하십시오.
* Data class를 적극 활용하십시오. Lombok은 사용하지 않습니다.
* 레거시의 member/, security/ 패키지를 참고하여 포팅하십시오.
* 새로운 코드를 작성하거나 수정할 때마다 반드시 단위 테스트(Unit Test)를 함께 작성하고 통과를 확인하십시오.
* 라이브러리 추가 시 업계 표준 및 Kotlin 친화적 의존성(예: mockito-kotlin) 사용 원칙을 준수하십시오.

### C. 메시징 및 이벤트 처리 규칙

* 모든 외부 이벤트 발행은 트랜잭셔널 아웃박스(Transactional Outbox) 패턴을 따릅니다.
* 이벤트 직렬화 포맷은 Apache Avro를 사용하며, Redpanda Schema Registry와 연동됩니다.
* 스키마 정의는 src/main/avro/ 경로에 .avsc 파일로 관리합니다.
* 스키마 변경 시 ./gradlew generateAvroJava 명령을 실행하여 최신 도메인 객체를 생성해야 합니다.
* 발행되는 메시지의 페이로드는 자동 생성된 Avro 클래스 인스턴스를 사용하십시오.

### D. 토큰 처리 규칙

* JWT Access Token과 Refresh Token 발급은 이 서비스가 담당합니다.
* 토큰 발급 후 쿠키에 설정하는 로직은 레거시의 AuthTokenService.java를 참고하십시오.
* Refresh Token은 Redis에 저장하고, tokenId를 키로 사용하십시오.
* 이 서비스 내에서 들어오는 일반 API 요청의 JWT를 검증하지 마십시오. X-Member-Id 헤더를 신뢰하십시오.

### E. OAuth 규칙

* Naver OAuth 연동 로직은 레거시의 AuthMemberServiceImpl.java를 참고하십시오.
* 외부 API 호출은 adaptor/out에 구현하고, 인터페이스(Port)를 통해 application 레이어와 통신하십시오.

### F. 데이터베이스 변경

* 스키마 변경 시 Flyway 마이그레이션 스크립트를 src/main/resources/db/migration에 작성하십시오.
* IF NOT EXISTS 구문과 기본값 설정으로 기존 데이터를 보호하십시오.

### G. 문서 규칙

* 마크다운 작성 시 굵게(bold)나 기울임(italics) 같은 강조 서식을 사용하지 않습니다.
* 마크다운 작성 시 이모티콘을 사용하지 않습니다.

## 5. 구현 스펙 참조

[docs/spec.md](./docs/spec.md)를 반드시 읽고 작업을 시작하십시오.
