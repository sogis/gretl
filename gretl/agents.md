# Agent Guide
This file is the shared briefing document for AI agents working in this
repository. Keep it current when the active task, test strategy, or coordination
rules change.

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

The integrator agent works on tasks spanning mor than one submodule (as described above)
Submodule agents work on subpackages only. They are allowed as exception to create 
the POJO DTO Objects in the parent package src/main/java/ch/so/agi/gretl/steps/publisher.

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


