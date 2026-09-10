# Live Coding

## Name the session
```
/rename nantes-craft
```

## Naïve first feature
```
create a call for paper (CFP) API that will manage proposals lifecycle for speaker to submit in a conference

first only add the proposal creation in the proposal domain, we will build the other features from here and there will be other domains

use java and Spring Boot to implement the API and use haxagonal port-adapter architecture and SOLID patterns

all the implementation has to be done through a test driven development loop

the current repository has been initialized with spring initializr to accelerate the setup
```

## Init the CLAUDE.md
```
/init
```

## Execute tests at each startup
```
add a hook that executes the test suite when a session begins and add a confirmation message into the conversation via systemMessage with the test results in a very short way (like X/X test passed)
```

## Create a first agent
```
create a `documenter agent responsible for maintaining ARCHITECTURE.md, README.md and CLAUDE.md with the useful information to understand how to use / contribute to this project and how and why it is built
do not try to update the documentation right after creating the agent, it needs to be reloaded into context
```
```
@documenter init the documentation 
```

## Install a skill : the skill-creator
```
! npx skills add https://github.com/anthropics/skills --skill skill-creator --agent claude-code
```
```
/reload-skills
```
## Use the skill creator to represent best practices
### TDD
```
create a skill that explicits the red green refactor tdd loop and best practices
```

### Hexagonal port-adapter architecture
```
/skill-creator extract the logic from hexagonal port-adapter architecture into its own skill
```

## Add deterministic tests to ensure architecture layout
```
in a dedicated subagent create an ArchUnit test suite corresponding to the architecture described by the hexagonal architecture skill and update the skill to refer to it
```

## Introduce Software Delivery LifeCycle workflow
### Specs
```
/skill-creator create a `sdlc-specs` skill that acts as an assistant for specifications of a  given feature

the assistant should ask questions about the least-specified parts when some parts of  the feature are under-specified or if the user asks for, in a 3-round question each time

the assistant should produce a `sdlc/XXX-my-feature.md` file structuring the feature in a business way, no technical aspects here unless they are business needs

create a dedicated git branch prior to start working on the feature
```

### Design
```
/skill-creator now the specs skill is written, we will create a `sdlc-design` skill

the `sdlc-design` skill will take a feature described by the specs skill and reviewed by the user and details the big steps to design this feature into the codebase

here the business needs should be represented by the technical actions battle plan to implement them and fulfil the needs

the skill should produce a `sdlc/XXX-my-feature-design.md` referencing an iterative path of the phases needed to implement of the feature, these phases should be as independent as possible to be implementable in parallel by leveraging hexagonal architecture decoupling
```

### Tasks
```
/skill-creator now the specs and design skills are here, we need to create a `sdlc-tasks` skill that will break the design down into very precise tasks

the task granularity should be as precise as possible, some examples are : TDD loop baby-steps, which files are touched, which method is modified...

the skill should output a sdlc/XXX-my-feature-tasks.md` file where tasks are represented line by line with an empty checkbox (so the implementation can just check the box when the task is done)

task list has to be organized like the design plan and should specify which tasks could be done in parallel to optimize implementation
```

### Implement
```
/skill-creator now the specs, design, tasks skills are written, we need an `sdlc-implement`` skill

this skill will use the task list in `sdlc/XXX-my-feature-tasks.md` and implement each tasks step by step, parallelizing when possible, ensure to work on a branch dedicated to the feature you work on

each time a task is finished, the implement skill should check the box corresponding to the implemented task

define a skill-local `implementer` agent related that will be used to implement the tasks as a sub agent and use git worktrees with a worktree name representing what the agent does when parallelizing
```

### Document the workflow
```
@documenter document the specs > design > tasks > implement workflow
```

## Commit the harness
```
commit this
```

## Implement a new feature with the SDLC skills
```
/clear
``` 
### Specify the Event feature
```
/sdlc-specs at the moment, proposals are not tied to a specific conference event, introduce the concept of event, to begin we just need to be able to create / list events and link proposals to an event in a simple way
```

### Design the feature into phases
```
/sdlc-design 001
```

### Split the design into actionable tasks
```
/sdlc-tasks 001
```

### Implement the feature in parallel
```
/sdlc-implement 001
```

### Ease the code review (bonus)
```
/code-review low level review of the Event feature using the diff with main branch
```

### Ease the Event feature documentation (bonus)
```
@documenter update the documentation according to the Event feature
```
