# microservice-member (QuietChatter Microservice)

이 저장소는 QuietChatter 프로젝트의 User 도메인을 담당하는 마이크로서비스입니다.
레거시 시스템(legacy-quiet-chatter)의 member, security 패키지 기능들을 코틀린(Kotlin)과 Spring Boot 3 환경으로 포팅하고 고도화하는 역할을 합니다.

## 아키텍처 및 역할

* 언어: Kotlin 1.9.x
* 프레임워크: Spring Boot 3.5.13
* 서비스 탐색: HashiCorp Consul (spring-cloud-starter-consul-discovery)
* 설정 관리: HashiCorp Consul (spring-cloud-starter-consul-config)
* 특징: 인증은 API Gateway에서 처리된 후 X-Member-Id HTTP 헤더를 통해 전달받습니다. 이 서비스 내부에서는 별도의 JWT 토큰 검증 로직을 구현하지 않습니다. 단, 최초 회원가입/로그인을 통한 토큰 발급 로직은 이 서비스에서 담당합니다.

## AI 에이전트 작업 지침

이 서비스에서 작업하기 전에 반드시 아래 문서를 먼저 읽으십시오.

1. [AGENTS.md](./AGENTS.md): 에이전트 작업 지침
2. [구현 스펙](./docs/spec.md): 이 서비스가 구현해야 할 기능 명세
