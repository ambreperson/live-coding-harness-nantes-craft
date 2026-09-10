---
name: sdlc-specs
description: Acts as a business analyst assistant to turn a feature idea into a structured specification document before any implementation starts. Use this whenever the user wants to spec, plan, or write requirements for a new feature — phrases like "let's spec out X", "write the requirements for X", "I want to add a feature that...", "before we build this, let's define it" — even if they jump straight to describing the feature without asking for a "spec" by name. Produces a business-only `sdlc/NNN-feature-slug.md` file (no technical/implementation detail) and creates a dedicated git branch for the feature before any work begins. Do NOT use this for writing code, architecture, or technical design docs — it stops at the business specification.
---

# Specifying a feature before it's built

Your job here is business analyst, not architect or developer: capture *what* the feature needs to do and *why*, for *whom*, and under what business constraints — never *how* it will be built. A spec written under this skill should be just as legible to a non-technical stakeholder as to an engineer. If you catch yourself writing a class name, an API shape, a database table, or a library, that content belongs in a future technical design, not here — cut it, unless the "technical" detail is actually a business requirement in disguise (e.g. "must comply with GDPR data retention rules" or "must support 10,000 concurrent submissions during the CFP deadline rush" are business needs; "use PostgreSQL" or "add a Redis cache" are not).

## Step 1 — Set up the branch first

Before drafting anything, create the dedicated branch for this feature. Working on a fresh branch from the start (rather than adding it after the spec is written) keeps the spec's own history clean and signals that "spec-writing" is itself the first unit of work on the feature, not a preamble to it.

1. Figure out the feature's slug: a short kebab-case name from the user's description (e.g. "let speakers withdraw a submitted proposal" → `withdraw-proposal`). Confirm it with the user if it's not obvious from their phrasing — a bad slug is annoying to rename later since it's baked into both the branch name and the filename.
2. Compute the next feature number: `bash .claude/skills/sdlc-specs/scripts/next-feature-number.sh sdlc` (defaults to `sdlc` — the script scans for the highest `NNN-*.md` already there and returns the next 3-digit number, or `001` if the directory doesn't exist yet).
3. Check the working tree state (`git status`) before branching. A few uncommitted changes aren't a blocker — they'll simply carry over to the new branch, which is normal `git` behavior — but if there's a large or unrelated pile of uncommitted work, flag it to the user rather than silently dragging it onto the new feature branch.
4. Create and switch to the branch: `git switch -c feature/NNN-slug` (or `git checkout -b` if `switch` isn't available), using the number from step 2 and the slug from step 1. Confirm the branch was created and checked out — don't assume the command succeeded silently.

## Step 2 — Assess what's already specified

Before asking anything, write out (for yourself) which of these business dimensions the user's request already answers, and which are missing or vague:

- **Problem / motivation** — why does this feature need to exist? What's broken or missing today?
- **Who it's for** — which user(s) or role(s) (e.g. speaker, organizer, reviewer) does this serve?
- **What "done" looks like** — the observable outcomes/success criteria, in business terms.
- **Core scenarios** — the main flows a user goes through, and how they're expected to behave (including the unhappy paths that matter to the business, e.g. "what happens if a speaker tries to withdraw after the CFP has closed?").
- **Business rules and constraints** — limits, policies, compliance, timing constraints, anything that shapes the feature but isn't a UI or implementation detail.
- **Explicit non-goals** — what this feature deliberately does NOT do, if the user has said so or it's a natural boundary worth pinning down.

A request can be under-specified in some dimensions and perfectly clear in others — don't ask about what's already answered. If everything above is reasonably answerable from what the user already said, you don't need to ask anything; go to Step 4 and write the spec, noting so briefly to the user. If the user explicitly asks you to interview them regardless (e.g. "ask me some questions about this first"), go to Step 3 even if the request looked complete.

## Step 3 — Ask in rounds, capped at three

When dimensions from Step 2 are genuinely unclear, ask about them using the `AskUserQuestion` tool, grouping up to 3 questions into one round (that tool takes 1-4 questions per call). Prioritize the *least*-specified areas first — the ones where you'd otherwise have to guess or invent business logic, not questions with an obvious sensible default.

- Run at most **3 rounds**. Each round should target what's still unclear after the previous one's answers — don't re-ask something already resolved, and don't pad a round with a question you could answer yourself with a reasonable default.
- Stop early, before 3 rounds, as soon as you have enough to write a coherent, unambiguous spec — more rounds than necessary just delays getting to a useful document.
- If, after 3 rounds, some dimension is still genuinely open, do not keep asking. Write the spec with your best reasonable assumption filled in, and list it explicitly under "Open questions" (see the template below) so it's visible and easy for a stakeholder to correct later, rather than silently baked in or blocking forever on an answer.

## Step 4 — Write `sdlc/NNN-feature-slug.md`

Use the feature number and slug from Step 1. Create the `sdlc/` directory if it doesn't exist. Structure the document like this — adapt section depth to how much there actually is to say (a small feature doesn't need padding to fill every section, and an empty section should just be omitted rather than left as a stub):

```markdown
# [Feature title]

## Problem / motivation
Why this is needed, in business terms.

## Users
Who this serves (roles/personas), and how each is involved.

## Scope
What this feature covers.

### Out of scope
What it deliberately does not cover (only include if relevant).

## Scenarios
The main flows, described in terms of user intent and business behavior, including
the unhappy/edge paths that matter to the business (e.g. limits, deadlines, permissions).
Prefer a Given/When/Then shape when it clarifies a rule, plain narrative otherwise —
don't force every scenario into Given/When/Then if it reads worse that way.

## Business rules & constraints
Policies, limits, compliance, timing constraints — anything that shapes correct
behavior but isn't a technical implementation choice.

## Success criteria
How the business will know this feature works / achieved its goal.

## Open questions
Anything still unresolved after the question rounds, with the assumption you made
in the meantime. Omit this section entirely if nothing is open.
```

Keep it readable by someone with no engineering background: no class/method/API names, no data model, no framework or library choices, no mention of this repo's architecture. If the feature genuinely requires a technical decision to even be describable at the business level (rare), phrase it as a constraint on the outcome, not the implementation.

## Step 5 — Wrap up

Tell the user: the branch you created, the file path you wrote, and — briefly — which sections you had to fill in with an assumption (if any, per the Open questions section) so they know exactly what to double check. Don't ask if they want to proceed to implementation; that's a separate, explicit step they'll ask for when ready (this skill's job ends at the spec).
