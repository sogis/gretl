# Agent Guide
This file is the shared briefing document for AI agents working in this
repository. Keep it current when the active task, test strategy, or coordination
rules change.

## 1. Task Context

Codex todos:
* Reduce the asking
  * Providing an implementation plan (what to include / exclude, to test)

Pending tasks:
* Modify the operation interface to allow nullable input and output paths $Coordinator
* Adapt all packages to the operation interface $Coordinator
* OK Implement in.xtf.tostage: transfer-file selection and import to stage.
* OK Implement stage.mergestages which merges all stage subdirectories into the new staged publication
* no Design stage.appendremote which appends parts from the sftp to the local stage to reach full data coverage even with partial publications
* Design stage.pushtoremote which publishes a new publication
* Design stage metainfo.m2m which publishes the publication info as digest of out.metainfo.table
* Design the ili model for out.metainfo.table

Responsibilities are documented in each package's `package-info.java` and in the
class-level Javadocs. Use this package map as the canonical module boundary:

Renamed package drafts:

- `in.db.dbToCache` is now `publisher.in.db.tocache`.
- `in.xtfToCache` is now `publisher.in.xtf.tocache`.
- Validation and user-format behavior are explicit modules:
  `publisher.cache.validation` and `publisher.cache.format`.

For the current Publisher work, start with:

- `ch.so.agi.gretl.steps.PublisherStepOld`

## 2. Repository Orientation

Expected content: summarize the project type, language, build system, important
source roots, and how the major parts fit together. Keep this short enough that
an agent can read it before touching code.

This repository is a Java 11 Gradle plugin project. Main code lives under
`src/main/java`, unit tests under `src/test/java`, integration tests under
`src/integrationTest/java`, and integration test jobs under
`src/integrationTest/jobs`.

## 3. Key Files And Ownership

For Publisher-related work, central files include:

- `src/main/java/ch/so/agi/gretl/tasks/Publisher.java`: Gradle task API,
  task properties, validation, and delegation into the implementation.
- `src/main/java/ch/so/agi/gretl/steps/PublisherStepOld.java`: core Publisher
  implementation for DB/file publication, validation, packaging, metadata,
  history, and grooming.
- `src/main/java/ch/so/agi/gretl/util/publisher/*.java`: publication payload
  model classes.
- `src/docs/publisher.qmd`: user-facing Publisher documentation.
- `src/docs/tasks/_ch.so.agi.gretl.tasks.Publisher.md`: generated/current
  Publisher parameter reference.
- `src/test/java/ch/so/agi/gretl/steps/*Publisher*`: focused Publisher tests.
- `src/integrationTest/java/ch/so/agi/gretl/jobs/PublisherTestOld.java` and
  `src/integrationTest/jobs/Publisher*`: Gradle-level Publisher integration
  tests and fixtures.

## 4. Build And Test Commands

Common commands:

```bash
./gradlew test
./gradlew test --tests '*Publisher*'
./gradlew integrationTest --tests '*Publisher*'
```
## 5. Coding Conventions

Expected content: document local style and compatibility rules that matter for
the task. Include Java version, Gradle API constraints, naming rules, backward
compatibility expectations, and whether old properties must remain supported.

Current baseline:

- Java source and target compatibility are Java 11.
- Write code following the principles described in the book "Clean Code" by Robert C. Martin (https://www.lkhibra.ma/books/clean-code.pdf)
- Apply widely accepted coding patterns
- Also consult patterns in neighboring task and step classes. Ask if they conflict good coding  
- When adding new task properties, update documentation and tests together.

## 6. Coordination Model

The integrator agent works on tasks spanning more than one publisher submodule
from the package map in section 1. Submodule agents work on their assigned
subpackage only. As an exception, they may create shared POJO/DTO objects in the
parent package `src/main/java/ch/so/agi/gretl/steps/publisher`.

## 7. Change Boundaries

Expected content: state what is in scope and out of scope. This prevents agents
from making unrelated refactors while solving a narrow task.

Agents should keep changes focused on the active task. Do not reformat unrelated
files, rename public APIs without a migration path, or change test fixtures that
are unrelated to the behavior under work.

## 8. Testing Expectations

For Publisher work, prefer focused unit tests around `PublisherStep` for core
behavior and integration tests when Gradle task configuration or public task
properties change.

## 9. Documentation Expectations

Publisher API or behavior changes should be reflected in `src/docs/publisher.qmd`
and, when applicable, the task parameter reference under `src/docs/tasks`.

## 10. Handoff Format

Use this format:

```text
Changed files:
Behavior changed:
Tests run:
Tests not run:
Open risks:
Suggested next step:
```

## 11. Open Questions And Decisions

* How to deal with basket ref's inside publisher.in.db.copy?
  * If the ref's exist on both sides, make the copy "basket-aware" by mapping the bid from source to target?
  * List as known issue and map dirty using the basket tid?
  * Use existing functionality "deeper" in ili2db/ioxili to transfer

Time: Do 4:40 - 6:45, 7:15 - 11:50, 12:40 - 18:10
Time: Fr: 5:40 - 6.50 8-9
