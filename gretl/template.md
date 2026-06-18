# Agent Guide

This file is the shared briefing document for AI agents working in the Folder
gretl/ of this repository.

## 1. Task Context

The goal is to modularize the code of the publisher step 

Modules (sub-packages under ch.so.agi.gretl.steps.publisher) after refactoring. New modules are marked with (NEW), 
deprecated Modules with (DEP):

* in.db.copy (NEW): Copies selected tables in a source schema to n target schemas with 
  identically named tables but less columns. Filters rows with an optional whereclause.
* in.db.dbToCache: 
  * Reads 1-n matching datasets from the specified database schema.
  * Writes
    * The 1-n xtf files corresponding to the read datasets to a configurable temp dir path
    * The information on the written datasets to a java data class (in memory)
* in.xtfToCache:
  * Reads 1-n matching xtf files from the specified folder.
  * Writes
    * The 1-n xtf to a configurable temp dir path
    * The information on the written datasets to a java data class (in memory)
* cache.append.data: Appends missing 
* cache.append.meta: Appends metainformation files to the written data files in the temp dir
* cache.metainfo: Adds metainformation files to the written data files in the temp dir
* out.repoupdate: Updates the data repo with the new files
* out.metainfo.table (NEW): Writes the Information on the published new data parts to the meta table
* out.metainfo.simi (DEP): Communicates with SIMI over a REST-API and writes the data description file

For the current Publisher work, start with:

- `ch.so.agi.gretl.steps.PublisherStepOld`

## 2. Repository Orientation

The folder gretl/ in this repository is a Java 11 Gradle plugin project. 
Main code lives under
`src/main/java`, unit tests under `src/test/java`, integration tests under
`src/integrationTest/java`, and integration test jobs under
`src/integrationTest/jobs`.

## 3. Key Files And Ownership

Expected content: list the files or directories that are central to the current
task, explain why each matters, and identify files that should not be edited by
more than one agent at the same time.

For Publisher-related work, central files include:

- `src/main/java/ch/so/agi/gretl/tasks/Publisher.java`: Gradle task API,
  task properties, validation, and delegation into the implementation.
- `src/main/java/ch/so/agi/gretl/steps/PublisherStep.java`: core Publisher
  implementation for DB/file publication, validation, packaging, metadata,
  history, and grooming.
- `src/main/java/ch/so/agi/gretl/util/publisher/*.java`: publication payload
  model classes.
- `src/docs/publisher.qmd`: user-facing Publisher documentation.
- `src/docs/tasks/_ch.so.agi.gretl.tasks.Publisher.md`: generated/current
  Publisher parameter reference.
- `src/test/java/ch/so/agi/gretl/steps/*Publisher*`: focused Publisher tests.
- `src/integrationTest/java/ch/so/agi/gretl/jobs/PublisherTest.java` and
  `src/integrationTest/jobs/Publisher*`: Gradle-level Publisher integration
  tests and fixtures.

## 4. Build And Test Commands

Expected content: provide exact commands agents should run for compilation,
focused tests, broader tests, and docs checks. Mention any commands that need
external services or credentials.

Common commands:

```bash
./gradlew test
./gradlew test --tests '*Publisher*'
./gradlew integrationTest --tests '*Publisher*'
```

Some tests use databases, SFTP, S3, or other external services. Check
`build.gradle` and `integration-test.gradle` before assuming those tests can run
locally without extra configuration.

## 5. Coding Conventions

Expected content: document local style and compatibility rules that matter for
the task. Include Java version, Gradle API constraints, naming rules, backward
compatibility expectations, and whether old properties must remain supported.

Current baseline:

- Java source and target compatibility are Java 11.
- Prefer existing patterns in neighboring task and step classes.
- Keep Gradle task APIs backward compatible unless the task explicitly requires
  a breaking change.
- When adding new task properties, update documentation and tests together.

## 6. Coordination Model

Expected content: describe how multiple agents should divide work, which branch
or worktree each owns, and which files are off limits for parallel edits.

Recommended workflow:

- Use one git worktree per agent.
- Give each agent a narrow ownership area.
- Avoid parallel edits to large shared files such as `PublisherStep.java`.
- Merge in small, reviewable steps: models first, task API next,
  implementation next, tests and docs last.

## 7. Change Boundaries

Expected content: state what is in scope and out of scope. This prevents agents
from making unrelated refactors while solving a narrow task.

Agents should keep changes focused on the active task. Do not reformat unrelated
files, rename public APIs without a migration path, or change test fixtures that
are unrelated to the behavior under work.

## 8. Testing Expectations

Expected content: define the minimum verification required before handoff. Note
which behavior needs unit tests, integration tests, regression fixtures, or
manual inspection.

For Publisher work, prefer focused unit tests around `PublisherStep` for core
behavior and integration tests when Gradle task configuration or public task
properties change.

## 9. Documentation Expectations

Expected content: explain which docs must be updated when APIs, task properties,
examples, file layouts, or behavior change.

Publisher API or behavior changes should be reflected in `src/docs/publisher.qmd`
and, when applicable, the task parameter reference under `src/docs/tasks`.

## 10. Handoff Format

Expected content: define the short status report every agent should provide at
the end of its work so integration is predictable.

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

Expected content: record unresolved design questions, decisions already made,
and the rationale for each decision. Keep this section concise and update it
instead of scattering decisions through chat transcripts.

For Publisher Next work, open questions and proposed decisions currently live in
`next_publisher.md`.
