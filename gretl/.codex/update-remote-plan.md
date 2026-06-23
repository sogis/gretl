# Update Remote Plan

This plan covers the refactor of the remote publication update flow into the
`ch.so.agi.gretl.steps.publisher.out.updateremote` package family.

## Goal

Implement a remote-first update flow that keeps merge and rotation on the remote
SFTP-backed filesystem, avoiding full local materialization of existing
publication state.

## Proposed API

Public entry point:

```java
public void update(
    Path remoteTargetRoot,
    String dataIdent,
    Date publishDate,
    Path localStageRoot
) throws Exception
```

Rules:

- `publishDate` is the source of truth for control flow
- `publishdate.json` is output-only metadata
- the remote updater must not depend on reading `publishdate.json`

## Responsibility Split

The existing update responsibilities are partitioned as follows:

- `1` -> `target`
- `(4,5)` -> `stage`
- `6` -> `seed`
- `8` -> `push`
- `(9,10)` -> `rotate`

## Package Layout

```text
updateremote/
  RemoteUpdater.java
  target/
  stage/
  seed/
  push/
  rotate/
```

### `updateremote`

Orchestrator only.

- Owns the end-to-end sequencing
- Wires the sub-steps together
- Contains no heavy filesystem logic

### Shared State Rule

Do not introduce a large shared `context` object.

Use the smallest possible data flow between steps:

- pass primitive values and `Path` objects explicitly
- keep each package responsible for its own local state
- create tiny step-specific DTOs only when a return value needs to be grouped
- avoid a single mutable object that accumulates all publication state

If a shared carrier is unavoidable, it must be read-only and minimal, for
example only containing the resolved target root and publication date.

### `updateremote.target`

Responsibility `1`.

- Validate the already resolved local or SFTP-backed target `Path`
- Keep this as the target boundary for later URI/filesystem setup if needed

Suggested signature:

```java
Path resolve(Path target);
```

### `updateremote.stage`

Responsibilities `(4,5)`.

- Create `<target>/<dataIdent>`
- Create the remote `dataRoot`
- Validate the target structure needed for update
- Create the temp staging directory

Suggested signature:

```java
RemoteStagePaths prepare(Path remoteTargetRoot, String dataIdent, Date publishDate);
```

Where `RemoteStagePaths` is a tiny read-only value object for:

- `dataRoot`
- `tempStageRoot`

Derived paths are strict global rules and must not be stored separately:

- `currentRoot = dataRoot.resolve("aktuell")`
- `historyRoot = dataRoot.resolve("hist")`

### `updateremote.seed`

Responsibility `6`.

- Seed the temp stage from the existing `aktuell`
- Copy existing `*.zip` files for incremental region updates
- Keep this limited to mirroring the current remote state into staging

Suggested signature:

```java
void seed(Path dataRoot, Path tempStageRoot) throws IOException;
```

### `updateremote.push`

Responsibility `8`.

- Push locally created data and metadata files into the remote temp stage
- Transfer generated ZIPs and metadata artifacts into the staging directory
- Keep file transfer separate from file generation
- Mirror the local stage tree into the remote temp stage without semantic file classification
- Preserve directory structure as-is, including `meta/`

Suggested signature:

```java
void push(Path localStageRoot, Path remoteTempStageRoot) throws IOException;
```

### `updateremote.rotate`

Responsibilities `(9,10)`.

- Delete existing `aktuell` when republishing the same date
- Move existing `aktuell` to `hist/<old-date>`
- Move temp to new `aktuell`
- Preserve any required cleanup of historical user-format files
- Prefer delete-then-move for same-date overwrite, not move-overwrite

Suggested signature:

```java
void rotate(Path dataRoot, Path tempStageRoot, Date publishDate) throws IOException;
```

## Implementation Checklist

1. Create the package skeleton
- Add `ch.so.agi.gretl.steps.publisher.out.updateremote`
- Add subpackages:
  - `target`
  - `stage`
  - `seed`
  - `push`
  - `rotate`

2. Define the data flow
- Prefer direct parameters and narrow return types over a shared context object
- Keep state localized to each step package
- Only introduce tiny read-only value objects when a step boundary requires it
- Do not introduce remote control flow dependencies on `publishdate.json`

3. Implement target resolution
- Add a `TargetResolver` in `target`
- Validate local or SFTP-backed destinations already passed as `Path`
- Keep URI parsing and filesystem initialization outside `RemoteUpdater`

4. Implement stage preparation
- Add a `StagePreparer` in `stage`
- Create the dataset root when needed
- Create the temp staging directory
- Keep stage preparation independent from `publishdate.json`
- Return only `dataRoot` and `tempStageRoot`

5. Implement seed logic
- Add a `StageSeeder` in `seed`
- Copy existing `aktuell/*.zip` files into the temp stage
- Use this only for incremental publications that need current coverage
- Derive `aktuell` from `dataRoot` inside the package

6. Implement push logic
- Add a `LocalToRemotePusher` in `push`
- Copy locally generated publication files into the remote temp stage
- Include both data and metadata artifacts that are produced locally
- Treat the local stage as the source of truth for file placement

7. Decide metadata generation boundaries
- Do not recreate a separate `meta` package
- Keep metadata generation in the upstream publication flow or inside the
  producer that prepares files for `push`
- Let `push` handle the transfer boundary only

8. Implement rotation
- Add a `LatestRotator` in `rotate`
- Move existing `aktuell` to `hist/<old-date>`
- Handle same-date overwrite by deleting the old `aktuell`
- Promote the temp stage to `aktuell`
- Use the `publishDate` argument, not a remote metadata read, for rotation naming
- Derive `aktuell` and `hist` from `dataRoot` inside the package
- Use delete-then-move for the overwrite branch to keep behavior deterministic

9. Wire the orchestration step
- Add `RemoteUpdater` in the root `updateremote` package
- Sequence:
  - resolve target
  - prepare stage
  - seed from current state if needed
  - push generated artifacts
  - rotate temp into place
  - clean up the temp stage on failure while it still exists

10. Add tests per responsibility
- Target resolution tests
- Stage validation tests
- Seeding behavior tests
- Push copy tests
- Rotation tests
- At least one full-flow integration-style test

11. Add failure-mode coverage
- Same-date overwrite
- `hist/<date>` collision
- Missing `publishdate.json`
- Partial update with seeded regions
- Rotation failure after staging
- Temp cleanup on failure

## Done Criteria

- The new update flow works on local and SFTP-backed targets
- Existing remote state is not fully downloaded just to merge or rotate
- The implementation is split according to the responsibility map above
- The tests cover the main success path and the important failure modes
- The code remains readable and aligned with the repository's current
  publisher architecture
