---
name: sdlc-tasks
description: Breaks an already-written technical design (produced by the sdlc-design skill, at `sdlc/NNN-feature-slug-design.md`) into a precise, checkbox-per-line task list ready to execute. Use this when the user says things like "break this design into tasks", "give me a task list for this feature", "turn this design into a todo list", or asks what to do next after a design exists. Produces `sdlc/NNN-feature-slug-tasks.md`: one checkbox per concrete action — exact file, exact class/method, exact TDD red/green/refactor micro-step — organized the same way as the design's phases, with parallelizable tasks called out explicitly. Do NOT use this to write the design itself (that's sdlc-design) or to actually implement the tasks (that's tdd-loop, working through this list one checkbox at a time).
---

# From design to an executable checklist

This is the last SDLC handoff before code: `sdlc-specs` decided the *what/why*, `sdlc-design` decided the *technical shape and phasing*, and this skill turns that phasing into a list precise enough that ticking every box means the feature is actually done — no judgment calls left for whoever executes it. Precision is the entire value of this document: a task like "implement the application service" is not a task by this skill's standard, because it hides several decisions (which method, tested how, in what order) inside one checkbox. Break it down until each box is a single, unambiguous, mechanical action.

## Step 1 — Read the design (and the spec behind it)

Find `sdlc/NNN-feature-slug-design.md` (ask the user which feature if unclear). Read it fully, and skim the source spec it references (`sdlc/NNN-feature-slug.md`) for the scenario/business-rule wording — task descriptions should stay traceable to *why* a task exists, not just *what* to type.

If the design has an "Open technical questions" section still unresolved, flag it to the user before turning it into tasks — a task list built on an unresolved technical decision will need rework the moment that decision is made, which defeats the point of a precise checklist.

## Step 2 — Decompose each phase/track into TDD micro-steps

For every domain-model change, port, application method, and adapter behavior the design calls for, write it out as the individual red → green → refactor steps from the `tdd-loop` skill — don't summarize the loop as one task, spell out each step as its own checkbox so progress is visible and nothing gets silently skipped (like the run-and-observe-red step, which is exactly the kind of thing a rushed implementer skips if it's not written down):

- **RED**: name the exact test method (in the exact test class/file) and what it asserts, then a task to run it and confirm it fails for the right reason (compilation error vs. assertion failure — see `tdd-loop`).
- **GREEN**: name the exact class/file and the exact method to add or change (e.g. "add `Proposal.withdraw()` to `domain/model/Proposal.java`, throwing `InvalidProposalException` if status is not `SUBMITTED`"), then a task to re-run the test and confirm green.
- **REFACTOR**: only include this as a separate task where the design or the emerging code plausibly needs cleanup (naming, duplication) — don't pad every single micro-step with a hollow "refactor" box that has nothing to actually refactor; when in doubt, one refactor+full-suite-rerun checkbox per track is enough rather than one per test.

For non-test-driven mechanical actions (adding a Maven dependency, creating a package-info, a config change), a single precise task is fine — the RED/GREEN/REFACTOR breakdown is specifically for behavior, not plumbing.

Use the `hexagonal-architecture` skill to get exact file paths right (which package, `domain/model` vs `domain/port/in` vs `application` vs `adapter/in/web` vs `adapter/out/persistence`) — every task's file path should match that layout, and reference existing sibling files in the codebase (e.g. how the `proposal` domain named things) so the task list reads consistently with what's already there.

## Step 3 — Mirror the design's phase structure, marking parallelism explicitly

Reuse the same Phase 0 / Phase 1 (tracks) / Phase 2 structure the design already established — don't re-derive or rename it. For each task or task group, make the parallelism the design identified visible directly on the checklist, not just implied by the section headers, since that's the point of asking for it:

- Under each Phase 1 track's heading, add a one-line note of what it may run in parallel with (e.g. "*(parallel with Track B, Track C — no shared files)*"), so someone scanning the list mid-implementation immediately sees what else could be picked up right now.
- If, while decomposing, you notice two tasks *within* the same phase or track don't actually touch the same file and have no ordering dependency between them (e.g. two independent domain exceptions in Phase 0), call that out too — parallelism opportunities aren't only at the track level the design already named; surface any additional ones you find at the file/task level.
- Conversely, if a task in a "parallel" track actually depends on another track's output (a design gap, not just an oversight in this step), flag it rather than silently ordering around it — it means the design's phase boundary needs revisiting.

## Step 4 — Write `sdlc/NNN-feature-slug-tasks.md`

Same `NNN` and slug as the design document (`-tasks` suffix). Every task is one checkbox line — `- [ ] <task>` — precise enough to execute without re-reading the design. Structure:

```markdown
# [Feature title] — Tasks

Source design: sdlc/NNN-feature-slug-design.md

## Phase 0 — Foundation
*(sequential — complete before starting Phase 1)*

- [ ] <precise task>
- [ ] <precise task>
...

## Phase 1 — Parallel tracks

### Track A — Application
*(parallel with Track B, Track C)*

- [ ] RED: write `XTest#should_...` in `path/to/XTest.java` asserting ...
- [ ] Run `./mvnw test -Dtest=XTest#should_...` — confirm it fails for the expected reason
- [ ] GREEN: add/modify `method(...)` in `path/to/X.java` to ...
- [ ] Run the test again — confirm it passes
- [ ] ...

### Track B — Web adapter
*(parallel with Track A, Track C)*

- [ ] ...

### Track C — Persistence adapter
*(parallel with Track A, Track B)*

- [ ] ...

## Phase 2 — Integration & verification
*(sequential — after every Phase 1 track is fully checked)*

- [ ] Run the full suite: `./mvnw test`
- [ ] Run the architecture rules specifically if this feature touched cross-layer dependencies: `./mvnw test -Dtest=HexagonalArchitectureTest`
- [ ] <manual/end-to-end verification tied to the spec's success criteria>
- [ ] <documentation update, if this feature changes something documented in README.md/ARCHITECTURE.md/CLAUDE.md — otherwise omit>
```

Keep every task a single checkbox — don't bundle "write and run the test" into one line if they're logically two actions (writing it and observing it run); the whole point of one-box-per-action is that partial progress is visible and resumable.

## Step 5 — Wrap up

Tell the user the file path, how many tasks landed in each phase/track, and anything flagged from Step 1 or Step 3. Don't start checking boxes yourself unless explicitly asked to begin implementing — this skill's job is producing the list, not executing it.
