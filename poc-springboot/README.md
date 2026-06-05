# poc-springboot

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#licença)

API REST mínima em Spring Boot com CRUD completo pro recurso **Person**, persistindo em PostgreSQL. Serve como template pra iniciar novos projetos Spring Boot com boas práticas já configuradas.

---

## Sumário

- [Features](#features)
- [Stack](#stack)
- [Pré-requisitos](#pré-requisitos)
- [Quick start](#quick-start)
- [Configuração](#configuração)
- [API](#api)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Testes](#testes)
- [Qualidade de código](#qualidade-de-código)
- [Docker](#docker)
- [Observabilidade](#observabilidade)
- [Convenções de log](#convenções-de-log)
- [Licença](#licença)

---

## Features

- CRUD completo com arquitetura em camadas (**Controller → Service → Repository**)
- Injeção via construtor (boa prática de IoC no Spring)
- Spring Data JPA — CRUD sem SQL boilerplate
- DTOs separados da entidade + **MapStruct** pra tradução
- Bean Validation (`@Valid`, `@NotBlank`, `@Email`)
- Tratamento global de erros com `@RestControllerAdvice`
- **Swagger/OpenAPI** documentando os endpoints
- Migrations versionadas com **Flyway**
- **Spring Boot Actuator** (health, info, metrics, prometheus)
- **Config por profile** (`dev`, `test`, `prod`) + credenciais via env var
- **Testcontainers** pros testes de integração (Postgres real)
- Formatação automática com **Spotless** + google-java-format
- **Dockerfile multi-stage** com layer caching do Spring Boot

## Stack

| Camada         | Tecnologia                              |
|----------------|-----------------------------------------|
| Linguagem      | Java 21                                 |
| Framework      | Spring Boot 4.0.5                       |
| Build          | Maven                                   |
| Persistência   | Spring Data JPA + PostgreSQL 16         |
| Migrations     | Flyway                                  |
| Mapeamento     | MapStruct 1.6                           |
| Documentação   | springdoc-openapi (Swagger UI)          |
| Observabilidade| Spring Boot Actuator + Micrometer       |
| Testes         | JUnit 5 + Testcontainers                |
| Format / Lint  | Spotless + google-java-format           |
| Container      | Docker multi-stage                      |

## Pré-requisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose

## Quick start

Clona, copia o `.env.example` e sobe:

```bash
git clone <repo-url> poc-springboot
cd poc-springboot
cp .env.example .env
```

**Opção A** — Postgres em container, app no host (dev loop mais rápido):

```bash
docker compose up -d postgres
mvn spring-boot:run
```

**Opção B** — tudo em container:

```bash
docker compose --profile app up --build
```

Sobe em `http://localhost:8080`. Flyway aplica as migrations automaticamente.

Acessos úteis:

| Recurso        | URL                                                |
|----------------|----------------------------------------------------|
| Swagger UI     | http://localhost:8080/swagger-ui.html              |
| OpenAPI JSON   | http://localhost:8080/v3/api-docs                  |
| Health         | http://localhost:8080/actuator/health              |
| Métricas       | http://localhost:8080/actuator/metrics             |
| Prometheus     | http://localhost:8080/actuator/prometheus          |

## Configuração

### Profiles

| Profile | Uso                  | DDL      | Logs                   | SQL log |
|---------|----------------------|----------|------------------------|---------|
| `dev`   | desenvolvimento      | validate | DEBUG + trace bindings | sim     |
| `test`  | testes de integração | validate | WARN                   | não     |
| `prod`  | produção             | validate | WARN / INFO            | não     |

Profile ativo vem de `SPRING_PROFILES_ACTIVE` (default `dev`).

### Variáveis de ambiente

Copia `.env.example` pra `.env` e ajusta:

| Variável                 | Default                                       | Descrição                    |
|--------------------------|-----------------------------------------------|------------------------------|
| `SPRING_PROFILES_ACTIVE` | `dev`                                         | Profile ativo                |
| `DB_URL`                 | `jdbc:postgresql://localhost:5432/pocdb`      | JDBC URL do Postgres         |
| `DB_USERNAME`            | `postgres`                                    | Usuário do banco             |
| `DB_PASSWORD`            | `postgres`                                    | Senha do banco               |
| `POSTGRES_DB`            | `pocdb`                                       | Nome do banco (docker)       |
| `POSTGRES_USER`          | `postgres`                                    | Usuário (docker)             |
| `POSTGRES_PASSWORD`      | `postgres`                                    | Senha (docker)               |

> **Em produção**: nunca deixe credencial hardcoded no `application.yml`. Só env var (ou secret manager).

### Migrations (Flyway)

Arquivos SQL em `src/main/resources/db/migration/` seguindo a convenção `V{n}__descricao.sql`. O Flyway aplica na ordem e marca como aplicado — uma migration já rodada não roda de novo.

```bash
mvn flyway:info      # estado das migrations
mvn flyway:migrate   # aplica pendentes (o Spring já faz isso no boot)
```

## API

Base URL: `http://localhost:8080`

| Método | Endpoint        | Status success | Descrição                |
|--------|-----------------|----------------|--------------------------|
| GET    | `/people`       | 200            | Lista todos              |
| GET    | `/people/{id}`  | 200 / 404      | Busca por id             |
| POST   | `/people`       | 201            | Cria                     |
| PUT    | `/people/{id}`  | 200 / 404      | Atualiza                 |
| DELETE | `/people/{id}`  | 204            | Remove                   |

### Schema

**Request / Response body**:

```json
{
  "name": "Ana",
  "email": "ana@mail.com",
  "phone": "11999999999"
}
```

**Regras**: `name` e `email` são obrigatórios; `email` precisa ter formato válido.

**Erro de validação** (`400`):

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-04-18T10:00:00",
  "errors": ["email: must be a well-formed email address"]
}
```

### Exemplos com curl

```bash
# cria
curl -s -X POST http://localhost:8080/people \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana","email":"ana@mail.com","phone":"999"}' | jq

# lista
curl -s http://localhost:8080/people | jq

# busca por id
curl -s http://localhost:8080/people/1 | jq

# atualiza
curl -s -X PUT http://localhost:8080/people/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana Lima","email":"ana@mail.com","phone":"000"}' | jq

# remove
curl -s -X DELETE http://localhost:8080/people/1 -o /dev/null -w "%{http_code}\n"
```

## Estrutura do projeto

```text
poc-springboot/
├── .editorconfig
├── .env.example
├── .dockerignore
├── .gitignore
├── Dockerfile                     ← multi-stage (build → runtime)
├── docker-compose.yml             ← postgres + app (profile opcional)
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/michael/poc/
    │   │   ├── Main.java
    │   │   ├── SwaggerConfig.java
    │   │   ├── controller/PersonController.java
    │   │   ├── service/PersonService.java
    │   │   ├── repository/PersonRepository.java
    │   │   ├── entity/Person.java
    │   │   ├── dto/ (PersonRequest, PersonResponse)
    │   │   ├── mapper/PersonMapper.java
    │   │   └── exception/ (ErrorResponse, GlobalExceptionHandler)
    │   └── resources/
    │       ├── application.yml           ← config comum
    │       ├── application-dev.yml
    │       ├── application-test.yml
    │       ├── application-prod.yml
    │       └── db/migration/V1__create_person_table.sql
    └── test/java/com/michael/poc/
        ├── PersonIntegrationTest.java    ← Testcontainers (Postgres real)
        ├── controller/PersonControllerTest.java
        ├── service/PersonServiceTest.java
        └── mapper/PersonMapperTest.java
```

## Testes

```bash
mvn test      # unit tests (rápidos, com mocks)
mvn verify    # unit + integração (Testcontainers) + spotless:check
```

A integração usa **Testcontainers** com `postgres:16-alpine`. Precisa do Docker rodando. O `.withReuse(true)` mantém o container entre runs — ative `testcontainers.reuse.enable=true` no seu `~/.testcontainers.properties` pra acelerar o loop local.

### Pirâmide de testes

- **Unit** — camadas isoladas com mocks (service, mapper, controller com `@WebMvcTest`)
- **Integração** — stack completa com Postgres real via Testcontainers

## Qualidade de código

### Spotless + google-java-format

```bash
mvn spotless:check   # só valida
mvn spotless:apply   # formata tudo
```

O `check` roda na fase `verify`, então `mvn verify` quebra o build se tiver código fora do padrão. Estilo: **google-java-format (GOOGLE)**, 2 espaços de indentação.

### EditorConfig

O `.editorconfig` garante consistência de encoding, line endings e indentação independente da IDE.

## Docker

### Build local

```bash
docker build -t poc-springboot:latest .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/pocdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  poc-springboot:latest
```

O Dockerfile é **multi-stage**:
1. **Build stage** — Maven compila e empacota
2. **Runtime stage** — só JRE + layered jar do Spring Boot

Usa o `layertools` do Spring Boot pra separar dependências, snapshots e classes da aplicação em camadas diferentes, aumentando o hit rate do cache do Docker em rebuilds.

## Observabilidade

Endpoints do Actuator expostos (configurável por profile):

- `/actuator/health` — liveness/readiness probes
- `/actuator/info` — metadados da app (inclui git info se disponível)
- `/actuator/metrics` — métricas via Micrometer
- `/actuator/prometheus` — scrape endpoint pro Prometheus

## Convenções de log

- **DEBUG** pras leituras no service (`findAll`, `findById`)
- **INFO** pras operações que mudam estado (`create`, `update`, `delete`) — só id, **nunca PII**
- **WARN** pros erros esperados (validação, 404)
- **ERROR** só pro inesperado (500), com stack trace

Sempre com placeholders SLF4J (`log.info("person created id={}", id)`) — nada de concatenação de string.

## Licença

MIT. Usa o código como quiser.
