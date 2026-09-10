---
name: hexagonal-architecture
description: Scaffolds and reviews code against this project's hexagonal (ports & adapters) architecture — one package per bounded-context domain, strict dependency direction toward the domain. Use this whenever adding a new domain (e.g. speaker, review), adding a new use case or port to an existing domain, adding a new adapter (a second `in` adapter, a different persistence technology, etc.), or reviewing/refactoring code for architectural compliance in this repo — even if the user just says "add a feature" or "add an endpoint" without naming the architecture explicitly, since every domain in this codebase must follow it.
---

# Hexagonal architecture in this codebase

This skill is the actionable, "how do I build/extend/check this" counterpart to [`ARCHITECTURE.md`](../../../ARCHITECTURE.md), which holds the full rationale, current package inventory, and dependency diagram — read it once if you haven't, but don't restate it here. This skill instead walks through *doing the thing*: scaffolding a new domain or use case correctly the first time, and catching violations when reviewing existing code.

Pair this with the `tdd-loop` skill when actually writing the code layer by layer — this skill tells you *where* each piece goes and *what may depend on what*; `tdd-loop` tells you *how* to build each piece test-first.

## The one rule everything else follows from

**Dependencies point inward, toward the domain.** The domain (`domain/model`, `domain/port`) never imports anything from `application` or `adapter`. Everything else in this skill is a consequence of that one rule — when you're unsure whether something belongs somewhere, ask "does this make the domain depend on something outside itself?" If yes, it's in the wrong place.

## Package layout (the template for every domain)

Every domain is a sibling package under `conf.live.cfp`, shaped identically:

```
conf.live.cfp.<domain>/
├── domain/
│   ├── model/        # aggregate(s), value objects, domain exceptions — plain Java, zero framework imports
│   └── port/
│       ├── in/        # use case interfaces + their command/request records — what the domain offers
│       └── out/        # interfaces the domain needs from the outside world (e.g. a repository) — what the domain requires
├── application/       # use case implementations (Spring @Service) — orchestrate domain + out ports
└── adapter/
    ├── in/web/                 # REST controllers, request/response DTOs, @RestControllerAdvice
    └── out/persistence/        # JPA entities, Spring Data repositories, adapter implementing the out port
```

## Adding a new domain from scratch

1. Create the package skeleton above under `conf.live.cfp.<newdomain>`.
2. Start at `domain/model`: design the aggregate as a plain Java class with a static factory (e.g. `Thing.create(...)`) that enforces invariants and throws a domain-specific exception on violation — no Spring/JPA/validation annotations anywhere in `domain/model` or `domain/port`. Add a `rehydrate(...)` factory once persistence needs to reconstruct the aggregate without re-validating.
3. Define the `domain/port/in` use case interface(s) plus their command/request record(s) — this is the contract the domain offers to the outside world.
4. Define the `domain/port/out` interface(s) — the contract the domain requires from infrastructure (typically a repository with the operations the domain actually needs, not a generic CRUD interface).
5. Implement `application`: a `@Service` implementing the `port/in` interface, depending only on `port/out` interfaces (constructor-injected) — never on a concrete adapter class.
6. Implement the adapters last, once the domain and application layer exist to drive them:
   - `adapter/out/persistence`: a JPA entity (its own model, not the aggregate), a Spring Data repository, and an adapter class implementing the `port/out` interface that is the *only* place translating between the aggregate and the entity.
   - `adapter/in/web`: a controller mapping HTTP DTOs to/from the `port/in` command and the aggregate, plus a `@RestControllerAdvice(assignableTypes = <Controller>.class)` mapping domain exceptions to `ProblemDetail` responses — scoped to the controller, not global, so exception handling stays local to the domain's web adapter.
7. Build all of this test-first, inside-out (domain → application → persistence adapter → web adapter) — see `tdd-loop`.

## Adding a use case to an existing domain

Same inside-out order, but narrower: add the new `port/in` interface/command, implement it in a new or extended `application` service, and only touch the adapters that need to expose it (e.g. a new controller endpoint, or none at all if it's only reachable from another use case). You do not need to touch domains you're not extending — a domain must only be reached through its own `port/in`, never by another domain reaching into its `model` or `adapter` packages.

## Adding a second adapter of an existing kind

The point of the architecture is that this should never require touching the domain or application layer. Examples: a CLI or message-consumer `in` adapter alongside the web one, or swapping/adding a persistence technology alongside JPA. Write the new adapter against the existing `port/in`/`port/out` interface — if you find yourself needing to change the port to accommodate the new adapter, that's a sign the port was modeling the *current* adapter's shape rather than the domain's actual needs; fix the port's contract instead of leaking adapter concerns into the domain.

## Reviewing code for compliance

Most of the dependency-direction rules below are **enforced automatically** by an ArchUnit test suite at `src/test/java/conf/live/cfp/architecture/HexagonalArchitectureTest.java`. Run it whenever you've touched imports/dependencies across layers, and always as part of the full suite before considering work done:

```bash
./mvnw test -Dtest=HexagonalArchitectureTest   # just the architecture rules, fast
./mvnw test                                     # full suite, includes these rules
```

It scans the whole `conf.live.cfp` codebase once (`@AnalyzeClasses(packages = "conf.live.cfp")`) and checks, one focused `@ArchTest` rule per concern:

- `domain_must_not_depend_on_spring` / `domain_must_not_depend_on_jpa` / `domain_must_not_depend_on_bean_validation` — no class in `domain/model` or `domain/port` may depend on `org.springframework..`, `jakarta.persistence..`, or `jakarta.validation..` (domain purity, split into three so a violation names the exact concern breached).
- `application_must_not_depend_on_adapters` — no `application` class may depend on `adapter` classes.
- `adapters_must_not_be_depended_on_by_domain_or_application` — dependencies only ever point inward: neither `domain` nor `application` may depend on `adapter`.
- `domains_must_not_reach_into_other_domains_internals` — a domain package may not depend on another domain's `.domain.model` or `.adapter` packages (only that other domain's `port.in`, if anything). Written generically over any `conf.live.cfp.<domain>` segment, so it actually fires once a second domain (e.g. `speaker`) exists, even though it's vacuously green with only `proposal` today.
- `spring_mvc_controller_annotations_are_confined_to_the_web_adapter` — any `@RestController`/`@RestControllerAdvice` must live under `adapter/in/web`.

When reviewing a diff or auditing code the test suite doesn't (or can't) mechanically check, also look for:

- **Adapter model leaking into the domain, or vice versa**: a JPA entity or HTTP DTO used directly as if it were the aggregate (skipping the adapter's translation step), or a domain aggregate exposed directly on the wire or in a persistence annotation. ArchUnit can catch the annotation/dependency symptoms above but not this kind of structural leak — it needs a human (or a review pass) to notice.
- **Validation duplicated or misplaced**: business invariants re-implemented in the controller (instead of relying on the aggregate's factory method) — Bean Validation at the HTTP boundary should only guard against malformed input, not stand in for domain rules.
- **A global exception handler** instead of one scoped to the controller it belongs to (`@RestControllerAdvice(assignableTypes = ...)`) — a global handler tends to accumulate cross-domain exception mappings that make it unclear which domain's rules produced which HTTP status.

If you find a violation — whether ArchUnit caught it or you spotted it by reading — fix it by moving the offending logic/dependency to the layer the rules above say it belongs in — don't just note it without proposing the correction, since the fix is almost always mechanical once the violation is named. If you add a new architectural rule while extending this skill, add a corresponding `@ArchTest` to `HexagonalArchitectureTest` in the same change, so the rule stays enforced rather than just documented.
