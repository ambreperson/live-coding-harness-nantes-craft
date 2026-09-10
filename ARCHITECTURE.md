# Architecture

This project follows a **hexagonal architecture** (ports & adapters), organized **per bounded-context domain**. The goal is to keep business rules (the domain) isolated from delivery mechanisms (HTTP, persistence, frameworks), so each can change or be tested independently.

## Package layout

Each domain lives under `conf.live.cfp.<domain>` and is structured the same way:

```
conf.live.cfp.<domain>/
├── domain/
│   ├── model/        # aggregate(s), value objects, domain exceptions — no framework dependency
│   └── port/
│       ├── in/        # use case interfaces + their command/request records
│       └── out/        # interfaces the domain needs from the outside world (e.g. a repository)
├── application/       # use case implementations (Spring @Service), orchestrate domain + out ports
└── adapter/
    ├── in/web/                 # REST controllers, request/response DTOs, @RestControllerAdvice
    └── out/persistence/        # JPA entities, Spring Data repositories, adapter implementing the out port
```

Only the `proposal` domain exists today, with a single use case (submitting a new proposal). Further domains (e.g. `speaker`, `review`) will be added as sibling packages under `conf.live.cfp`, each with the same internal shape.

## Dependency direction

```
adapter/in/web  ──┐
                   ├──▶ domain/port/in  ◀── application ──▶ domain/port/out ◀──┐
                   │         │                                                  │
                   │         ▼                                                  │
                   └──▶ domain/model                              adapter/out/persistence
```

The rule that must always hold: **dependencies point inward, toward the domain.** Concretely:

- `domain/model` and `domain/port` are plain Java: no Spring, no JPA, no web annotations. They express business rules and only depend on the outside world through interfaces they define (`port/out`).
- `application` implements the `port/in` use case interfaces and depends only on `port/out` interfaces — never on a concrete adapter class. Spring wires the concrete adapter in at runtime via dependency injection.
- Adapters (`adapter/in/web`, `adapter/out/persistence`) depend on the domain, never the other way around. Each adapter translates between its own model (JPA entity, HTTP DTO) and the domain aggregate; the domain aggregate itself never appears on the wire or in a persistence schema directly.

This means the domain can be unit-tested with zero Spring context, and any adapter (e.g. swapping JPA for another persistence technology, or adding a second `in` adapter such as a CLI or a message consumer) can be replaced without touching business logic.

## Current state: the `proposal` domain

The only domain implemented so far is `proposal`, with one use case: **submit a new proposal**.

- **`domain/model/Proposal`** — the aggregate root. Immutable, identified by a generated UUID. Two factory methods:
  - `Proposal.submit(title, description, speakerId)` — enforces invariants (none of the three fields may be blank) and throws `InvalidProposalException` on violation. Produces a proposal in `DRAFT` status.
  - `Proposal.rehydrate(...)` — reconstructs an aggregate from persisted data, without re-validating (invariants were already checked at submission time).
- **`domain/model/ProposalStatus`** — currently only `DRAFT`; more statuses (`SUBMITTED`, `ACCEPTED`, `REJECTED`, ...) will be added alongside the use cases that transition between them.
- **`domain/port/in/SubmitProposalUseCase`** + **`SubmitProposalCommand`** — the use case contract and its input.
- **`domain/port/out/ProposalRepository`** — the persistence contract the domain needs (`save`).
- **`application/SubmitProposalService`** — implements the use case: builds a `Proposal` via the domain factory, then delegates persistence to the `ProposalRepository` port.
- **`adapter/in/web/ProposalController`** — exposes `POST /api/proposals`, mapping `SubmitProposalRequest` (validated with Bean Validation) to a `SubmitProposalCommand`, and the resulting `Proposal` to a `ProposalResponse`, returning `201 Created` with a `Location` header.
- **`adapter/in/web/ProposalExceptionHandler`** — a `@RestControllerAdvice(assignableTypes = ProposalController.class)` that maps `InvalidProposalException` to a `400` `ProblemDetail`. Scoping the advice to the controller keeps HTTP concerns local to this domain's web adapter rather than global.
- **`adapter/out/persistence/ProposalEntity`** + **`ProposalJpaRepository`** + **`ProposalRepositoryAdapter`** — the JPA-backed implementation of `ProposalRepository`. `ProposalRepositoryAdapter` is the only place that translates between `Proposal` and `ProposalEntity`.

## Design rationale

- **Domain purity as a rule, not a suggestion**: keeping `domain/model` and `domain/port` free of framework annotations means the aggregate's invariants can be tested with plain JUnit + AssertJ, with no Spring context startup cost, and means the business rules cannot silently start depending on infrastructure behavior (e.g. lazy-loading, transaction boundaries).
- **Validation lives in the aggregate factory, not the controller**: Bean Validation (`@Valid` on `SubmitProposalRequest`) only guards the HTTP boundary (e.g. rejecting genuinely malformed JSON payloads early, with a generic `400`); the actual business invariants are enforced once, in `Proposal.submit(...)`, so any future `in` adapter (a CLI, an event consumer) gets the same guarantees for free.
- **`rehydrate` vs `submit`**: separating aggregate reconstruction from creation avoids re-running validation logic (and its exceptions) every time an aggregate is read back from storage — validation is a one-time gate at the business operation that created the data.
- **One package per domain**: this keeps future domains (e.g. `speaker`, `review`) independent and prevents a shared "god package"; a domain can only reach into another domain's internals through its `port/in` (use cases) and never through its `model` or `adapter` packages.

See [CLAUDE.md](CLAUDE.md) for the condensed, day-to-day version of these rules and the exact test commands.
