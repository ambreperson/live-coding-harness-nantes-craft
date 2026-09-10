# CFP API

A REST API managing the lifecycle of talk proposals submitted by speakers for a conference's Call For Paper (CFP).

Built with Java 25 and Spring Boot 4.1.1 (Maven), following a hexagonal (ports & adapters) architecture — see [ARCHITECTURE.md](ARCHITECTURE.md) for the design details.

## Prerequisites

- Java 25 (JDK)
- No local database setup needed: the app uses an in-memory H2 database.

The project ships with the Maven Wrapper (`./mvnw`), so a local Maven install isn't required.

## Running locally

```bash
./mvnw spring-boot:run
```

The API starts on the default Spring Boot port (8080). A proposal must reference an existing event, so create one first:

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"name": "Nantes Craft"}'
# -> note the returned "id", e.g. "event-id"

curl -X POST http://localhost:8080/api/proposals \
  -H "Content-Type: application/json" \
  -d '{"title": "Hexagonal architecture in practice", "description": "A deep dive into ports and adapters", "speakerId": "speaker-1", "eventId": "event-id"}'
```

## Building and testing

```bash
./mvnw test                                   # run the full test suite
./mvnw test -Dtest=ProposalTest                # run a single test class
./mvnw test -Dtest=ProposalTest#should_create_a_draft_proposal_with_the_given_data  # single test method
./mvnw clean verify                            # full build (compiles, runs tests, packages)
```

## Contributing

This codebase is developed **test-first (TDD)**: write a failing test, then the minimal code to make it pass, then refactor. Every architectural layer has its own kind of test (domain unit tests, application unit tests with Mockito, persistence tests, `@WebMvcTest` web tests) — see [ARCHITECTURE.md](ARCHITECTURE.md) and [CLAUDE.md](CLAUDE.md) for the details of what to test where.

Test classes must be named `*Test` (not `*IT`): only `*Test`/`Test*`/`*Tests`/`*TestCase` are picked up by the configured Surefire plugin.

Commit messages in this repo follow the [Gitmoji](https://gitmoji.dev/) convention (e.g. `:sparkles:`, `:bug:`, `:memo:`).
