# HireFlow – Recruitment / Applicant Tracking Service

A Spring Boot 4 (Java 17) backend where recruiters post jobs, candidates apply, and applications move
through a hiring pipeline. **MySQL** is the system of record, **Kafka** drives asynchronous screening
and candidate notifications, and **Redis** handles caching and per-candidate rate limiting.

```
            REST (Spring MVC)                       Kafka topic: hireflow.hiring-events (3 partitions, key = applicationId)
 client ──▶ JobController / CandidateController ─┐        ▲                         │
            ApplicationController                │        │ AFTER_COMMIT            ├─▶ ScreeningListener   (group hireflow-screening)
                                                 ▼        │                         │     scores skills + experience → applications.match_score
                                     ApplicationService ──┘                         └─▶ NotificationListener (group hireflow-notifications)
                                       │        │                                         idempotent by eventId → notifications table
                                       │        └── Redis: rate limit (INCR + EXPIRE)     failures: 3 retries w/ backoff → hireflow.hiring-events.DLT
                                       ▼
                                 MySQL 8 (Flyway)     Redis cache: jobs (10 min TTL), pipeline (30 s TTL)
```

## What each technology does here

| Concern | Implementation |
|---|---|
| **MySQL + Flyway** | Schema in `V1__create_recruitment_schema.sql`; `ddl-auto=validate`. Unique keys enforce one application per candidate per job and idempotent notifications. Composite indexes for the pipeline and listing queries. |
| **JPA tuning** | `@EntityGraph` loads job + candidate in one query (no N+1 on listing endpoints); the pipeline dashboard uses an interface **projection** with `GROUP BY` instead of loading entities. Optimistic locking with `@Version`. |
| **Kafka** | Events are published only **after the DB transaction commits** (`@TransactionalEventListener`). Two consumer groups receive every event independently. `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` with exponential backoff; `ErrorHandlingDeserializer` keeps poison messages from blocking a partition. Idempotent producer, `acks=all`. |
| **Redis** | `@Cacheable` job details and pipeline counts with per-cache TTL and JSON serialization; explicit eviction on writes. Fixed-window **rate limiter** (max N applications per candidate per hour) shared across instances. |
| **API quality** | Bean Validation, RFC 9457 `ProblemDetail` errors, OpenAPI/Swagger UI, Actuator health. |
| **Testing** | JUnit 5 + Mockito unit tests, `@WebMvcTest` slice tests, and an end-to-end **Testcontainers** test with real MySQL, Kafka and Redis. |

## Run it

```bash
docker compose up -d
```

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd`. Then open http://localhost:8080/swagger-ui.html.

Try the flow:

```bash
curl -X POST localhost:8080/api/jobs -H "Content-Type: application/json" -d "{\"title\":\"Java Backend Engineer\",\"department\":\"Engineering\",\"location\":\"Bengaluru\",\"minExperienceYears\":3,\"skills\":[\"java\",\"spring boot\",\"kafka\",\"redis\"]}"
curl -X POST localhost:8080/api/candidates -H "Content-Type: application/json" -d "{\"fullName\":\"Asha Rao\",\"email\":\"asha@example.com\",\"experienceYears\":4,\"skills\":[\"java\",\"spring boot\"]}"
curl -X POST localhost:8080/api/applications -H "Content-Type: application/json" -d "{\"jobId\":1,\"candidateId\":1}"
curl localhost:8080/api/applications/1                # matchScore filled in by the Kafka consumer
curl -X PATCH localhost:8080/api/applications/1/stage -H "Content-Type: application/json" -d "{\"stage\":\"SCREENING\"}"
curl localhost:8080/api/candidates/1/notifications
curl localhost:8080/api/jobs/1/pipeline
```

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/jobs` | Post a job |
| GET | `/api/jobs?skill=&page=&size=` | Search open jobs (paged) |
| GET | `/api/jobs/{id}` | Job details (Redis cached) |
| POST | `/api/jobs/{id}/close` | Close a job |
| GET | `/api/jobs/{id}/applications?stage=` | Applicants ranked by match score |
| GET | `/api/jobs/{id}/pipeline` | Count per stage (Redis cached) |
| POST | `/api/candidates` | Register a candidate |
| GET | `/api/candidates/{id}/applications` | A candidate's applications |
| GET | `/api/candidates/{id}/notifications` | Messages produced by the Kafka consumer |
| POST | `/api/applications` | Apply (rate limited, 429 when exceeded) |
| PATCH | `/api/applications/{id}/stage` | Move stage; invalid jumps return 422 |
| GET | `/api/applications/{id}/history` | Stage audit trail |

Pipeline: `APPLIED → SCREENING → INTERVIEW → OFFER → HIRED`, and any active stage can go to `REJECTED` or `WITHDRAWN`.

## Test

```bash
./mvnw test
```

The Testcontainers integration test needs Docker running; without Docker it is skipped and the unit
and slice tests still run.

## Possible next steps

- **Transactional outbox**: today an event is lost if Kafka is down at commit time; an outbox table plus a relay would make delivery guaranteed.
- Resume upload to S3 with skill extraction, JWT/OAuth2 security with recruiter and candidate roles, and a CI pipeline (GitHub Actions).
