# Milestone 2 — Phase 3 Completion Report

Format per `cross_platform_movement_detection_master_plan.md` Section 9.
Plan: `milestone_2_phase_3_plan.md`.

**Completed work:** Built a deterministic, camera-free movement-processing
test harness in `shared`, in a new `com.snowboardpose.shared.pose.replay`
package (the first subpackage under `pose/`, a deliberate departure from
Phase 2's flat layout): `PoseSequenceParseException` (uniform failure type
for unusable pose sequence JSON, extending `IllegalArgumentException` per
this codebase's existing validation convention), `PoseSequenceLoader`
(pure `String -> List<SkeletonFrame>` JSON parser — no file/disk/network IO
— that stably sorts by `timestampMillis` and rejects empty sequences),
`PoseFrameSink` (a `fun interface` placeholder pipeline contract standing in
for Phases 4–7's real normalization/filtering/detection), and
`RecordedSequenceReplayer` (a stateless, synchronous, deterministic
frame-by-frame driver returning captured `MovementEvent`s). Added
`PoseFixtures` in `commonTest` — one idle sequence and one synthetic
movement sequence, each as an in-memory `List<SkeletonFrame>` plus a
`PoseJson`-derived JSON string — per the user's confirmed decision to use
embedded Kotlin constants rather than real files on disk (the repo has no
multiplatform resource-loading mechanism; real file-based import is
deferred to Phase 12). All new code compiles and its tests pass on both
`shared` targets (android, jvm); `androidApp` still assembles against the
updated `shared` module. No new dependencies were added.

**Created files:**
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceParseException.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceLoader.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseFrameSink.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/RecordedSequenceReplayer.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseFixtures.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceLoaderTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/RecordedSequenceReplayerTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceReplayIntegrationTest.kt`
- `Plan Files/milestone_2_phase_3_plan.md` (this phase's plan record)

**Modified files:**
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseModelFixtureIntegrationTest.kt`
  — KDoc updated to note the real loader now exists at
  `pose.replay.PoseSequenceLoader`, and to explain why this Phase 2 test
  still hand-builds its own fixture rather than using it (keeps Phase 2's
  model-level coverage independent of Phase 3's parser). No test logic
  changed.
- `Plan Files/milestone_roadmap.md` — updated the Milestone 2 status row to
  reflect Phase 3 completion.

**Deleted files:** None.

**Dependencies added:** None. Reused existing `kotlinx-serialization-json`
(1.11.0) and `kotlin-test`.

**Unit tests executed** (all under `:shared:jvmTest` and
`:shared:testAndroidHostTest`, identical suite, 0 failures):
- `PoseSequenceLoaderTest` (8): valid JSON array decodes correctly;
  out-of-order input returns ascending-by-timestamp output; duplicate
  timestamps preserve stable relative order (not rejected); missing
  `timestampMillis` key rejected; malformed JSON syntax rejected; wrong
  field type rejected; non-array JSON root rejected; empty array rejected
  — all rejections via `PoseSequenceParseException`.
- `RecordedSequenceReplayerTest` (7): frames delivered to the sink in
  order; `previous` is `null` on the first frame and the prior frame
  thereafter; only non-null sink events are collected; two replays of the
  same input produce identical output (determinism); an empty frame list
  returns no events without invoking the sink; frame count delivered to
  the sink matches input size.

**Integration tests executed:**
- `PoseSequenceReplayIntegrationTest` (3), `commonTest`, run identically
  under both targets:
  - *Recorded file loads into `SkeletonFrame` objects*:
    `PoseSequenceLoader.loadJson(PoseFixtures.idleFixtureJson)` decodes to
    the exact expected frame list.
  - *Loaded sequence passes through a placeholder pipeline* / *expected
    number of frames reaches the detector*: the loaded idle sequence is
    replayed through a counting `PoseFrameSink`; frame count delivered
    matches `PoseFixtures.idleFrames.size`.
  - *Output events are captured correctly* / *a known fixture produces a
    known output summary*: a fixed placeholder sink emits exactly one
    `MovementEvent(HOP, ...)` at the known peak frame of the synthetic
    movement fixture; the resulting `List<MovementEvent>` is asserted
    exactly.

**System tests executed:** *Command-line or test-runner workflow processes
a complete fixture*: satisfied via the "test-runner workflow" alternative
the master plan's own wording allows — `PoseSequenceReplayIntegrationTest`
running under both `:shared:jvmTest` and `:shared:testAndroidHostTest`
(this repo has no CLI module; `settings.gradle.kts` includes only
`:shared`/`:androidApp`), documented explicitly in the test's KDoc, same
mapping technique Phase 2 used for its Android-adapter integration test.
`./gradlew build` — full project, including `androidApp` debug and release
variants, assembles successfully against the updated `shared` module.

**Performance tests executed:** None — not required this phase (no
per-frame heavy computation exists yet; `PoseFrameSink` test doubles are
trivial), per the plan. Becomes meaningful from Phase 5 onward.

**Build commands:**
```
./scripts/test.sh
./gradlew build
```
(`JAVA_HOME=/snap/android-studio/current/jbr`, same as Milestone 1/Phase 2
precedent; `scripts/test.sh` auto-detects it.)

**Build results:** `BUILD SUCCESSFUL` for both `./scripts/test.sh` and
`./gradlew build`.

**Test results:** 11 test classes / 57 tests passed under `:shared:jvmTest`;
the same 11 classes / 57 tests passed under `:shared:testAndroidHostTest`
(identical suite, both targets) — 0 failures, 0 skipped, on the first run
(no fix-up iterations needed).

**Manual validation:** Not applicable this phase — no UI-visible or
runtime-observable behavior change; `./gradlew build`'s success (including
`androidApp:assembleRelease`) is the system-test-equivalent verification
per the plan's scope.

**Known limitations:**
- Real, on-disk, device-recorded MediaPipe JSON is never exercised this
  phase — only hand-built fixtures in the same shape `PoseJson` produces.
  If a real recording has a shape `PoseJson`/`SkeletonFrame` doesn't
  tolerate, that will surface in Phase 12 (first phase with real file
  import), not now.
- CSV parsing is explicitly out of scope (JSON only, per the master plan's
  "JSON or CSV" — an "or," not "and" — and to avoid an unrequested second
  format).
- Duplicate-timestamp handling (stable-sort, not rejected) is a deliberate
  policy choice; Phase 5's plan must be written with awareness that
  duplicates may reach it unfiltered.
- `PoseFrameSink` is a placeholder only — no real normalization, filtering,
  or detection logic exists yet (Phases 4–7).
- No CLI module or file-based fixture loading exists; both are explicitly
  deferred to Phase 12.

**Problems encountered (and how they were resolved):** None — no build,
test, or environment issues arose this phase (the repo-configured
GateGuard fact-forcing hook denied-then-allowed each new file's first
Edit/Write attempt as in Phase 2, handled inline per file with brief
fact statements; not a code or test issue).

**Regression tests added:** None — no pre-existing defect was fixed; this
phase only added new scaffolding (consistent with Milestones 1/2's
precedent).

**Recommended next phase:** Phase 4 — Pose Validation and Normalization
(Milestone 2), per `milestone_roadmap.md`. Per the master plan's process,
implementation does not begin automatically — Phase 4 needs its own
presented, approval-pending plan first.
