---
name: tdd-loop
description: Enforces the strict red-green-refactor TDD workflow this project requires (see CLAUDE.md) for every feature, use case, or bug fix. Use this whenever implementing new behavior in this codebase, when the user asks to "do TDD," add a feature, fix a bug, or extend a domain (proposal, or any future domain) — even if they don't say "TDD" explicitly, since it's the mandatory process here, not an optional style choice.
---

# Red-Green-Refactor in this codebase

TDD here is not "write tests eventually" — it's a discipline where the test is the thing that tells you what to build, and you never write production code the test didn't ask for. Each of the three phases has a job; skipping or blending them is what causes both under-tested code (skipping red) and over-built code (skipping the discipline of minimal implementation).

## The loop

### 1. Red — write a failing test, then prove it fails for the right reason

Write one test for one new behavior, named so its intent is obvious from the name alone (`should_reject_a_blank_title`, not `test1` or `testValidation`). Then **run it and read the failure** before writing any production code:

```bash
./mvnw test -Dtest=ProposalTest#should_reject_a_blank_title
```

Two different failures mean two different things — tell them apart:
- **Compilation failure** (`cannot find symbol`): expected when the test references a class/method that doesn't exist yet. This is a valid "red" — it proves the test can't pass by accident. Move straight to green.
- **Assertion failure at runtime**: the code compiles but produces the wrong behavior. This is also valid red, but if you expected a compilation failure and got a passing test instead, stop — either the behavior already exists, or the test isn't actually exercising what you think it is. A test that passes before you've written the implementation is not testing anything.

Never write the implementation "preemptively" because you're confident what the test needs — the run-and-observe step is what catches a mistyped assertion, a test that isn't wired up, or a false assumption about existing behavior.

### 2. Green — the minimal code to pass, nothing more

Write just enough production code to make the failing test pass — not the full feature you have in mind, just what this one test demands. Three ways to get there, in increasing order of sophistication, pick whichever fits:
- **Fake it**: return the literal expected value. Legitimate for the very first test on a new piece of behavior — it forces you to write the next test that breaks the fake.
- **Obvious implementation**: when the real logic is trivial and low-risk, just write it directly.
- **Triangulation**: when the right generalization isn't obvious from one example, add a second test with different inputs and let the two examples force the general solution.

Resist adding validation, branches, or fields the current test doesn't require, even if you know the next test will need them — that's what the *next* red step is for. Run the test (and the full suite) to confirm green:

```bash
./mvnw test -Dtest=ProposalTest
./mvnw test
```

### 3. Refactor — improve structure, change no behavior

Only refactor from green. Clean up duplication, naming, or structure in either the test or the production code, but don't add new assertions or new production logic in this step — if you notice missing behavior while refactoring, write it down and go back to step 1 for it instead of sneaking it in here. Re-run the full suite after refactoring, not just the test you touched — a refactor can break something else even when the target test still passes:

```bash
./mvnw test
```

A practical corollary: don't edit a test and its implementation in the same step. If a test needs to change, that's either a new red (the old assertion was wrong or incomplete — change the test, watch it fail, then adjust the implementation) or a pure refactor of the test's structure with no behavior change. Changing both at once means you're no longer verifying anything.

## Best practices that keep the loop honest

- **One behavior per test.** If a test's name needs "and" to describe it, split it. Small, focused tests make the red phase actually diagnostic — a failure tells you exactly what's broken.
- **Small increments.** Prefer many small red-green-refactor cycles over one big one. If you're several methods deep into an implementation before running a test, you've left the loop.
- **Test names describe behavior, not mechanics.** `should_create_a_draft_proposal_with_the_given_data`, not `testSubmit`. The name should make the intent readable in a failure report without opening the file.
- **The test list**: before starting, it can help to jot down the behaviors you expect to need (valid case, each invalid input, edge cases) as a checklist — then take them one at a time through the full loop rather than trying to handle them all in one pass.

## How this maps onto this project's layers

This repo's hexagonal structure (see `ARCHITECTURE.md`) means "one behavior" usually lives at a specific layer, and each layer has its own established way of isolating it — check `CLAUDE.md`'s Testing section for the authoritative list, summarized here:

| Layer | Test style | What red/green looks like |
|---|---|---|
| `domain/model` | Plain JUnit 5 + AssertJ, no Spring context | Fast: red is usually a compile error (new factory method/class) or an assertion on a thrown domain exception |
| `application` | Mockito mocking the `domain/port/out` interfaces | Red proves the service doesn't yet call/orchestrate the port correctly; green is verified via `verify(...)`/`when(...)` |
| `adapter/out/persistence` | Mockito unit test for entity mapping, plus `@DataJpaTest` for a real H2 round-trip | Do the Mockito mapping test first (fast loop), then confirm with the slower `@DataJpaTest` before moving on |
| `adapter/in/web` | `@WebMvcTest` with the use case mocked via `@MockitoBean` | Red is typically a 404/500 because the endpoint or DTO doesn't exist yet; green asserts status + JSON body |

Run just the layer you're working on while iterating (fast feedback), and the full suite before considering the feature done:

```bash
./mvnw test -Dtest=ProposalTest                      # single class, tight loop
./mvnw test -Dtest=ProposalTest#should_reject_a_blank_title  # single method, tightest loop
./mvnw test                                          # full suite, before moving to the next layer or finishing
```

Remember the project's test naming rule: classes must be named `*Test` (not `*IT`) — Surefire's default include pattern won't pick up an `*IT` class since there's no Failsafe plugin configured, so a test named that way would silently never run and give you false confidence.

## When implementing a full feature top to bottom

For a use case that spans layers (e.g. a new command handled by domain → application → persistence → web, following the existing `proposal` submission as a template), run the full red-green-refactor loop independently at each layer, inside-out: domain model first (it has no dependencies to fake), then application service (mocking the port out), then the persistence adapter, then the web adapter. Don't write the web controller before the application service exists to call — each layer's tests should only ever depend on layers you've already built and proven.
