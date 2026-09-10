---
name: sdlc-implement
description: Executes an already-written task list (produced by the sdlc-tasks skill, at `sdlc/NNN-feature-slug-tasks.md`) — implementing each task on the feature's dedicated branch, checking its checkbox off as it's actually completed, and running Phase 1's independent tracks in parallel via dedicated git worktrees and implementer subagents. Use this when the user says things like "implement this feature", "let's build this now", "work through the task list", "start Track A/B/C", or asks to execute/continue a feature whose tasks file already exists. Do NOT use this to write the spec, design, or task list themselves (that's sdlc-specs / sdlc-design / sdlc-tasks) — this skill only executes an existing one.
---

# Executing the task list

This is the last step of the `sdlc-specs` → `sdlc-design` → `sdlc-tasks` → **`sdlc-implement`** chain: turning a checklist someone (or a previous run of this skill) can trust into actual committed code, one checkbox at a time. The task list was already broken into precise TDD micro-steps by `sdlc-tasks` — your job here is disciplined execution and orchestration, not re-deriving what to do.

## Step 0 — Locate the task list and the feature branch

Find `sdlc/NNN-feature-slug-tasks.md` (ask which feature if unclear). Confirm you're on the feature branch it belongs to (`feature/NNN-feature-slug`, created back in `sdlc-specs`) with `git branch --show-current` — switch to it if you're elsewhere, and if the branch doesn't exist at all, stop and tell the user the chain wasn't followed (this skill assumes `sdlc-specs` already created it) rather than inventing one.

Read the whole task list before touching anything, so you know the full Phase 0 / Phase 1 (tracks) / Phase 2 shape up front.

## Step 1 — Phase 0, done directly and sequentially

Phase 0 is foundational and, by `sdlc-design`'s own logic, meant to be small — there's no value in worktree/subagent overhead for something inherently linear. Work through its tasks yourself, in order, exactly as written (they're already precise TDD steps: which test to write, which command to run, which method to add). After each task is *actually* done — the test genuinely run and observed, the code genuinely written — flip its `- [ ]` to `- [x]` in `sdlc/NNN-feature-slug-tasks.md` immediately, not in a batch at the end, and commit at a granularity that mirrors the task list (small, frequent commits — one per red/green pair is reasonable). Checking a box before the work behind it is real defeats the entire purpose of the list as a trustworthy progress record.

Do not proceed to Phase 1 until every Phase 0 box is checked and committed — Phase 1's tracks depend on Phase 0's contracts existing.

## Step 2 — Phase 1: parallel tracks via worktrees + implementer subagents

If Phase 1 has only one track, or the tracks are trivially small, it's fine to just do them yourself sequentially like Phase 0 — don't spin up worktrees and subagents for parallelism that wouldn't actually save anything. Otherwise, for each track:

1. **Derive a track slug** from its heading in the task list (e.g. "Track A — Application" → `application`; "Track B — Web adapter" → `web-adapter`). This slug is what makes the worktree/branch name legible — it should describe *what the agent does*, not just "track-a".
2. **Create the worktree**, branched from the feature branch's current commit (i.e. after Phase 0 is committed):
   ```bash
   git worktree add .claude/worktrees/<track-slug> -b feature/NNN-feature-slug/<track-slug>
   ```
   `.claude/worktrees/` is already gitignored for this purpose.
3. **Spawn one `Agent` call per track, all in the same message** so they genuinely run concurrently (independent tool calls in one turn run in parallel) — don't spawn them one at a time across separate turns. For each:
   - `subagent_type: general-purpose`
   - `name`: something like `implement-<track-slug>`, so it's addressable and its identity in any status view reflects what it's doing
   - `prompt`: the content of `agents/implementer.md` (read it and include it), followed by: the absolute path to that track's worktree, and the exact checkbox lines for that track copied verbatim from `sdlc/NNN-feature-slug-tasks.md` (so the subagent isn't re-parsing the whole file and can't accidentally wander into another track's tasks).

## Step 3 — As each track reports back

You'll get a completion notification per track (they finish independently, not necessarily together). For each:

- **Update the checkboxes**: for every task the subagent says it completed, flip that exact line to `- [x]` in `sdlc/NNN-feature-slug-tasks.md` on the feature branch (not in the worktree) and commit that update, referencing the track in the commit message. Only check what was reported done — if a task is missing from its report, leave it unchecked.
- **If the subagent reports a blocker or an incomplete task**: don't silently mark it done or guess a resolution — surface it to the user. A design/task gap discovered mid-implementation (e.g. a port contract that didn't actually fit the track's need) is exactly the kind of thing worth stopping for rather than working around silently.
- **If the subagent's own tests weren't clean**: treat that track as not actually finished, even if it reported "done" — don't merge a track whose own verification failed.

## Step 4 — Merge tracks back into the feature branch

Once a track is confirmed complete and green, merge it in and clean up:

```bash
git merge feature/NNN-feature-slug/<track-slug>
git worktree remove .claude/worktrees/<track-slug>
git branch -d feature/NNN-feature-slug/<track-slug>
```

A genuine merge conflict here is a signal worth calling out explicitly rather than just resolving and moving on: hexagonal decoupling means independent tracks should touch disjoint files, so a conflict usually means the design's phase boundaries leaked (two tracks needed the same file after all) — mention this to the user even after resolving it, since it's useful feedback for `sdlc-design` next time. You can merge tracks in any order as they complete; you don't need to wait for all of them before merging the ones that are already done.

## Step 5 — Phase 2, done directly and sequentially

Once every Phase 1 track is merged, run Phase 2's tasks yourself the same way as Phase 0: one at a time, checking boxes as they're genuinely completed, committing as you go. This is normally the full-suite run, the architecture-rule check, and whatever end-to-end verification the task list specifies against the spec's success criteria.

## Step 6 — Wrap up

Report: which boxes are checked (ideally all of them), the feature branch name, and anything flagged along the way (a blocked task, a merge conflict that revealed a design gap, a track that needed rework). Don't open a pull request or merge the feature branch into the default branch unless the user explicitly asks for that — this skill's job ends at a fully-implemented, fully-tested feature branch.
