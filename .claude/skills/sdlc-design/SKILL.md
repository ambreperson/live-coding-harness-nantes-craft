---
name: sdlc-design
description: Turns an already-written, user-reviewed business specification (produced by the sdlc-specs skill, at `sdlc/NNN-feature-slug.md`) into a technical implementation battle plan. Use this when the user says things like "let's design how to build this", "translate the spec into a technical plan", "what's the implementation plan for [feature]", or asks to move a spec forward toward implementation. Produces `sdlc/NNN-feature-slug-design.md`: for every business need in the spec, the concrete technical action that fulfills it, organized into phases that are as independent as possible so they can be implemented in parallel by exploiting this repo's hexagonal architecture. Do NOT use this to write a business spec from scratch (that's sdlc-specs) or to write the actual production code (that's tdd-loop, applied once this design exists).
---

# Designing the technical path from spec to code

This skill sits between `sdlc-specs` (the business "what and why") and actual implementation (`tdd-loop` + `hexagonal-architecture`, the "how, one test at a time"). Its job is the missing middle step: for every business need in the spec, name the concrete technical action that fulfills it, and sequence those actions into phases — most of which should be doable in parallel, because that's the whole point of the hexagonal architecture this codebase uses (see `hexagonal-architecture` skill and `ARCHITECTURE.md`): once the domain contract is fixed, the pieces around it don't need each other's implementation to proceed.

## Step 1 — Locate and read the spec

Find `sdlc/NNN-feature-slug.md` (ask the user which feature if it's not obvious, or list `sdlc/*.md` and infer from context). Read it fully — the design must be traceable back to it, not invented independently.

If the spec still has an unresolved "Open questions" section, don't silently pick your own answer for something that changes the technical shape (e.g. a business rule that determines whether a new aggregate state is needed). Surface it to the user before proceeding: designing on top of an admittedly-open business question just relocates the ambiguity into code. Minor open points that don't affect the technical plan (e.g. copy text, a non-functional preference) can be noted and skipped.

## Step 2 — Translate business needs into technical actions

Go through the spec's Scenarios and Business rules & constraints section by section, and for each one decide which layer of the hexagonal architecture it lands in (per the `hexagonal-architecture` skill's package layout — consult it now if you haven't):

- A business rule that constrains what a valid state looks like (a limit, a required field, an invariant) → **domain model**: an aggregate invariant enforced in a factory method, or a new domain exception.
- A new capability the outside world can trigger → **`domain/port/in`**: a new use case interface + command, implemented in **`application`**.
- A new thing the domain needs from the outside world (persisting something new, looking something up) → **`domain/port/out`**: a new or extended repository-style interface.
- How a user actually reaches the capability → **`adapter/in/web`** (typically a new endpoint / DTO), or another `in` adapter if the spec calls for one.
- How data is actually stored/retrieved → **`adapter/out/persistence`** (entity/schema changes, the adapter implementing the new `port/out` method).
- A pure UX/business-process detail with no new technical surface (e.g. "organizers should see a clear message") → note it as a requirement on the relevant adapter's behavior, not a new architectural piece.

Don't invent technical solutions the spec doesn't call for — if a scenario doesn't require a new aggregate state, don't add one for "completeness." The design should be the *minimum* technical shape that satisfies every business need named in the spec, nothing more.

## Step 3 — Organize into phases, maximizing parallelism

The reason hexagonal architecture matters here: once the **domain model and port contracts** (interfaces + commands, not their implementations) are fixed, everything that depends on them — the application service, the web adapter, the persistence adapter — only needs the *contract*, never each other's code. That's what lets them proceed independently. Structure the plan around that fact:

- **Phase 0 — Foundation (sequential, blocks everything else).** The domain model changes (aggregate/invariants/exceptions) and the port interfaces (`port/in` commands + interfaces, `port/out` interfaces) this feature needs. Keep this phase small and fast — it's the one thing that must land before parallel work can start, so the longer it takes, the longer everything else waits. Nothing in a later phase should require changing a Phase 0 contract; if you find yourself wanting to, that's a sign Phase 0 wasn't fully thought through — better to catch it now than after parallel work has started against the old contract.
- **Phase 1 — Parallel tracks (independent once Phase 0 lands).** List each track that can proceed on its own against the Phase 0 contracts alone:
  - *Application track*: implement the use case service against `port/out` (test with Mockito mocks — it never needs the real persistence adapter to be done).
  - *Web track*: implement the controller/DTOs against `port/in` (test with `@WebMvcTest` and a mocked use case — it never needs the real application service to be done).
  - *Persistence track*: implement the entity/repository adapter against `port/out` (test with the Mockito mapping test + `@DataJpaTest` — it never needs the application or web track to be done).
  - Any other `in`/`out` adapter the spec requires, following the same principle.
  Name these as literal parallel tracks in the document (e.g. "Track A", "Track B") so it's obvious multiple people (or multiple work sessions) could pick up different tracks at the same time.
- **Phase 2 — Integration & verification.** Once the tracks land, this is normally just confirming the wiring (Spring DI resolving the right beans) and running the full suite — hexagonal architecture means there's usually no real "integration coding" left, just verification. Include here whatever end-to-end check proves the spec's success criteria are actually met (e.g. a manual `curl` walkthrough, or a full-stack test if the spec's scenarios warrant one).

If the feature is small enough that Phase 0 and a single Phase 1 track are basically the same amount of work, say so plainly rather than padding the document with phases that don't add real parallelism — the value of this structure is honest parallelism, not a fixed template to fill in.

## Step 4 — Write `sdlc/NNN-feature-slug-design.md`

Use the same `NNN` and slug as the source spec (same feature, `-design` suffix on the filename). Structure it like this, adapting depth to the feature's actual size:

```markdown
# [Feature title] — Technical design

Source spec: sdlc/NNN-feature-slug.md

## Domain model changes
What's new or modified in domain/model and domain/port, and why (tie back to the business rule it enforces).

## Ports
### In (new/changed use cases)
### Out (new/changed repository-style needs)

## Phase 0 — Foundation
Domain + port changes to land first (sequential, small).

## Phase 1 — Parallel tracks
### Track A — Application
### Track B — Web adapter
### Track C — Persistence adapter
(one subsection per track that can proceed independently; each says what it implements
and against which Phase 0 contract, so it's clear no track needs another track's code)

## Phase 2 — Integration & verification
What confirms the feature actually works end-to-end, tied back to the spec's success criteria.

## Traceability
A short table or list mapping each scenario/business rule from the spec to where it's
handled in this plan, so a reviewer can check nothing was dropped.

## Open technical questions
Anything you had to flag back to the user in Step 1, or a technical trade-off worth a second opinion. Omit if none.
```

## Step 5 — Wrap up

Confirm you're on the feature's branch (created by `sdlc-specs`, normally `feature/NNN-slug` — check with `git branch --show-current` and flag it if you're not, rather than silently writing the design file on the wrong branch). Tell the user the file path, a one-line summary of the phase breakdown, and anything from Step 1 you flagged. Don't start implementing — that's the next explicit step, using `tdd-loop` and `hexagonal-architecture` against this design.
