# MiniCoin

Spring Boot 기반의 학습용 블록체인·암호화폐 미니 프로젝트입니다. 블록체인의 구조를 REST API로 시각화하고, 이후 노드 간 동기화와 간단한 Proof-of-Work를 확장할 수 있도록 설계되었습니다.

## 구현 개요
- **REST 블록체인 시각화**: 체인에 포함된 블록 요약/세부 정보를 제공하는 `GET /api/blocks` 및 `GET /api/blocks/{hash}` 엔드포인트를 구현했습니다.
- **도메인 모델 구성**: `Block`, `Transaction` 레코드로 블록 데이터와 거래 정보를 표현하며, `BlockchainService`가 체인 상태를 관리합니다.
- **샘플 체인 데이터**: 제네시스 블록을 포함한 3개의 블록을 메모리에 초기화하여 학습 및 API 확인이 가능합니다.
- **노드 간 체인 동기화**: `NodeSynchronizationService`를 통해 주기적으로 피어 노드의 체인을 조회하고, 브로드캐스트로 전달된 블록을 검증·적용합니다.
- **OpenAPI 문서화**: `springdoc-openapi`를 적용해 Swagger UI(`/swagger-ui/index.html`)로 API를 탐색할 수 있습니다.

## 프로젝트 구조
```
src/main/java/study/cryptochain/minicoin
 ├─ MiniCoinApplication.java      # Spring Boot 실행 진입점
 ├─ config/OpenApiConfig.java     # Swagger/OpenAPI 기본 정보
 ├─ model/Block.java              # 블록 도메인 모델
 ├─ model/Transaction.java        # 트랜잭션 도메인 모델
 ├─ service/BlockchainService.java# 샘플 체인 및 조회 로직
 └─ web
     ├─ BlockchainController.java # 블록체인 REST Controller
     └─ dto/*.java                # API 응답 DTO
```

## REST API
| Method | Path | 설명 |
|--------|------|------|
| `GET`  | `/api/blocks` | 체인에 포함된 블록의 요약 정보 목록 제공 |
| `GET`  | `/api/blocks/{hash}` | 특정 해시 블록의 세부 정보 제공 |
| `GET`  | `/api/nodes/chain` | 현재 노드의 전체 체인 데이터 제공 |
| `POST` | `/api/nodes/blocks` | 다른 노드가 브로드캐스트한 블록 수신 |
| `POST` | `/api/nodes/sync` | 즉시 피어 노드와 동기화 수행 |
| `POST` | `/api/nodes/broadcast` | 특정 블록을 피어 노드로 전파 |

Swagger UI: [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)  
OpenAPI 문서: `/v3/api-docs`

## 개발 환경
- Java 21 (Gradle Kotlin DSL이 Java 25를 인식하지 못하므로 Java 21 사용 권장)
- Spring Boot 3.5.6
- Gradle 8 (Wrapper 포함)

## 노드 동기화 설정
`src/main/resources/application.properties`에서 노드 식별자와 브로드캐스트/동기화 옵션을 관리합니다.

```properties
minicoin.node.id=node-1
minicoin.node.port=8080
minicoin.node.peers[0]=http://localhost:8081
minicoin.node.sync-interval=PT30S
minicoin.node.sync-retry-attempts=2
server.port=${minicoin.node.port}
```

- `minicoin.node.peers`에 동기화 대상 노드의 베이스 URL을 배열 형태로 등록합니다.
- `minicoin.node.sync-interval`은 스케줄러 주기를 ISO-8601 Duration 포맷으로 설정합니다.
- `minicoin.node.sync-retry-attempts` 값만큼 네트워크 오류 시 재시도를 시도합니다.

## 실행 방법
1. Java 21이 설치되어 있는지 확인하고, `JAVA_HOME` 또는 `org.gradle.java.home`를 Java 21 경로로 설정합니다.
2. 의존성 설치 및 컴파일
   ```bash
   ./gradlew build
   ```
3. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```
4. 브라우저에서 `http://localhost:8080/swagger-ui/index.html` 접속 후 API를 확인합니다.

## 향후 확장 계획
- `PROJECT_CHECKLIST.md`의 체크리스트를 기준으로 노드 간 블록 동기화, Proof-of-Work 기반 채굴 엔드포인트, 테스트 및 모니터링 환경을 순차적으로 구현할 수 있습니다.
