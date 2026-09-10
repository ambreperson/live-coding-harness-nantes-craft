# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

CFP (Call For Paper) API: manages the lifecycle of talk proposals submitted by speakers for a conference. Java 25 / Spring Boot 4.1.1 (Maven). See [README.md](README.md) for setup/run and [ARCHITECTURE.md](ARCHITECTURE.md) for the full design rationale.

`live-coding.md` is intentionally blocked from Read/Bash access by `.claude/settings.json` — never try to work around this.

## Commands

```bash
./mvnw test                                  # run the full test suite
./mvnw test -Dtest=ProposalTest               # run a single test class
./mvnw test -Dtest=ProposalTest#should_create_a_draft_proposal_with_the_given_data  # single test method
./mvnw spring-boot:run                        # run the API locally
./mvnw clean verify                           # full build
```

The app uses an in-memory H2 database (no external DB setup needed to run or test).

## Architecture

Hexagonal (ports & adapters), one package per bounded-context domain under `conf.live.cfp.<domain>` (`domain/model`, `domain/port/in|out`, `application`, `adapter/in/web`, `adapter/out/persistence`). Two domains exist so far: `event` (create/list events) and `proposal` (submit a proposal, which requires a valid `eventId`); more domains (e.g. speaker, review) will follow the same structure. Full layering, dependency-direction rules and rationale: [ARCHITECTURE.md](ARCHITECTURE.md).

Rules that most affect how you write code here:
- `domain/model` and `domain/port` must stay free of Spring/JPA/web annotations.
- Domain invariants are enforced in aggregate factory methods (e.g. `Proposal.submit(...)`, `Event.create(...)`), which throw a domain exception (e.g. `InvalidProposalException`, `InvalidEventException`); a separate `rehydrate(...)` factory reconstructs an aggregate from persistence without re-validating.
- `application` services depend only on `domain/port/out` interfaces, never on adapter classes. The one cross-domain exception: `SubmitProposalService` depends directly on `event`'s `EventRepository` (`port/out`) to check the referenced event exists, throwing `event`'s `EventNotFoundException` if not — see [ARCHITECTURE.md](ARCHITECTURE.md) for the rationale.
- A `@RestControllerAdvice(assignableTypes = ...)` scoped to the controller maps domain exceptions to HTTP `ProblemDetail` responses — don't add a global exception handler. `ProposalExceptionHandler` maps both `InvalidProposalException` and `event`'s `EventNotFoundException` to `400`.

## Testing

TDD is the required workflow for this codebase: write a failing test first, then the minimal implementation to pass it, then refactor before moving on. Every layer is tested independently:
- `domain/model`: plain JUnit 5 + AssertJ unit tests, no Spring context.
- `application`: Mockito-based unit tests mocking the `domain/port/out` interfaces.
- `adapter/out/persistence`: a Mockito-based unit test for entity mapping, plus a `@DataJpaTest` for a real round-trip against H2.
- `adapter/in/web`: `@WebMvcTest` with the use case mocked via `@MockitoBean`.

Test classes must be named `*Test` (not `*IT`): Surefire's default include pattern only picks up `*Test`/`Test*`/`*Tests`/`*TestCase`, and this project has no Failsafe plugin configured, so an `*IT` class would silently never run.

Spring Boot 4.1 moved some test-slice annotations to new packages compared to earlier Spring Boot versions — notably `@DataJpaTest` is under `org.springframework.boot.data.jpa.test.autoconfigure` and `@WebMvcTest` under `org.springframework.boot.webmvc.test.autoconfigure`.
