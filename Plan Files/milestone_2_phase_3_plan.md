# Phase 3 — Recorded Pose Input and Test Harness

## Context

`Plan Files/milestone_roadmap.md` groups the master plan's 21 phases into 6
milestones. Milestone 2 (Phases 2–7) builds all movement-detection logic
inside the `shared` KMP module, with zero camera/platform dependency, fully
testable on desktop/CI. Phase 2 (shared pose data model — `JointType`,
`Joint`, `SkeletonFrame`, `MovementType`, `MovementEvent`,
`PoseProcessingConfig`, `PoseJson`) is implemented and reported on (see
`Plan Files/milestone_2_plan.md` / `milestone_2_phase_2_completion_report.md`).
Phase 3 is next per the roadmap and per the Phase 2 completion report's
"Recommended next phase." Its master-plan spec lives in
`Plan Files/cross_platform_movement_detection_master_plan.md` lines 550–591
(duplicated in `milestone_roadmap.md` lines 108–126): build a deterministic
movement-processing test harness independent of live camera input — load
recorded pose sequences, replay them in timestamp order, feed them into the
processing pipeline, and support deterministic tests, with at least one idle
and one synthetic-movement fixture usable.

Per the master plan's unchanged process, this phase is individually
inspected, planned, presented, and approved before any files change,
implemented only once approved, then tested and reported on — the agent
stops before starting Phase 4 automatically.

One design decision was confirmed with the user before finalizing this plan:
- **Fixture storage:** fixtures are embedded Kotlin constants (a
  `List<SkeletonFrame>` in `commonTest`, serialized to JSON text via the
  existing `PoseJson` instance), not real files on disk. The repo has no
  multiplatform resource-loading mechanism today (no `commonTest` resources
  source set, no okio/kotlinx-datetime anywhere), and this phase's loader
  only ever needs to accept a JSON `String` — real file/disk import of actual
  recorded device sequences is deferred to Phase 12 ("Pose Recording and
  Export"), the first phase with a genuine need to read files off disk. This
  avoids inventing multiplatform IO plumbing this phase doesn't otherwise
  need.

---

## Current repo state (verified directly)

- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/` contains the
  Phase 2 model, confirmed byte-for-byte against source:
  - `JointType.kt` — 33-entry MediaPipe-ordinal `@Serializable enum class`.
  - `Joint.kt` — `x`/`y`/`z`/`visibility`, `visibility` validated `[0,1]` via
    `require()`.
  - `SkeletonFrame.kt` — `timestampMillis: Long` (**required, no default**),
    `joints`/`worldJoints: Map<JointType, Joint> = emptyMap()`, with
    `joint()`/`worldJoint()`/`hasJoint()` accessors. No ordering/uniqueness
    enforcement on `timestampMillis` in the type itself — Phase 2 explicitly
    left that for Phase 3.
  - `MovementType.kt` — `NONE`/`HOP`/`TURN_AROUND`.
  - `MovementEvent.kt` — `type`/`timestampMillis`/`confidence`, confidence
    validated `[0,1]`.
  - `PoseProcessingConfig.kt` — visibility threshold + smoothing alpha.
  - `PoseJson.kt` — the one shared `Json` instance
    (`ignoreUnknownKeys = true`, `encodeDefaults = true`); its own KDoc
    already names "Phase 3 replay files" as an intended consumer, to be
    reused directly, not duplicated.
- `shared/build.gradle.kts`: KMP `android` + `jvm` targets only (no iOS —
  deferred to Milestone 4). `commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }`,
  `commonTest.dependencies { implementation(libs.kotlin.test) }`. No
  resources source set configured anywhere.
- `gradle/libs.versions.toml`: kotlin 2.4.10, kotlinx-serialization-json
  1.11.0, kotlin-test. No kotlinx-datetime, no okio anywhere in the repo.
- `settings.gradle.kts`: only `:shared` and `:androidApp` — no CLI module.
- Test conventions (all flat under
  `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/`, one
  `<Type>Test.kt` per production file, pure `kotlin.test`, no
  Kotest/JUnit/mocking):
  - `PoseSerializationTest.kt` uses `PoseJson.encodeToString`/`decodeFromString`
    plus `buildJsonObject{}`/`decodeFromJsonElement` for hand-built
    malformed-shape JSON — the direct precedent for this phase's
    malformed-input tests.
  - `PoseModelFixtureIntegrationTest.kt`'s KDoc says verbatim: *"There is no
    file-based loader yet (that is Phase 3's job)... this test builds an
    in-memory fixture directly with the shared models."* It hand-builds a
    5-frame `List<SkeletonFrame>` with strictly increasing `timestampMillis`
    (`index * 33L`) — the pattern this phase's real loader/replay tests
    extend, confirmed by direct read.
- `scripts/test.sh` runs `./gradlew :shared:jvmTest :shared:testAndroidHostTest --console=plain`
  (both KMP test targets together; `--full` also runs `./gradlew build`).
  This is "the normal test command" the master plan's Phase 3 completion
  criteria refers to.
- Repo has zero git commits (pre-existing from Milestone 1, unrelated to
  this phase).

---

## Phase 3 plan — Recorded Pose Input and Test Harness

**Phase:** 3 (Milestone 2, second of six)

**Objective:** Build a deterministic, camera-free movement-processing test
harness in `shared`: parse a recorded pose sequence from JSON text into
ordered `SkeletonFrame`s, and replay that sequence through a placeholder
processing pipeline, synchronously and reproducibly, with hand-built
fixtures usable by this phase and reusable by Phases 4–7.

**Current state:** See above — only the Phase 2 data model exists; no
loader, no replay harness, no fixtures.

**Scope:**
- New package `com.snowboardpose.shared.pose.replay`, `commonMain`/
  `commonTest` only. This is the first subpackage under `pose/` — a
  deliberate departure from Phase 2's flat layout (replay/harness is a
  distinct concern from the core data model, and Phases 4–7 will add several
  more such concerns), flagged so it's read as an intentional precedent.
- `PoseSequenceParseException` — one exception type for every "this JSON is
  not a usable pose sequence" failure mode.
- `PoseSequenceLoader` — pure `String -> List<SkeletonFrame>` parser, no
  file/disk/network IO. Sorts ascending by `timestampMillis` (stable),
  rejects empty sequences, wraps all `SerializationException`s.
- `PoseFrameSink` — `fun interface` placeholder pipeline contract standing
  in for Phases 4–7's real normalization/filtering/detection.
- `RecordedSequenceReplayer` — stateless, synchronous, deterministic driver:
  `replay(frames, sink) -> List<MovementEvent>`.
- `PoseFixtures` (`commonTest` only) — one idle sequence, one
  synthetic-movement sequence, each as an in-memory `List<SkeletonFrame>`
  plus a `PoseJson.encodeToString(...)`-derived JSON string (not hand-typed
  JSON, so fixtures can't drift from the real wire format).
- Hand-written malformed-JSON string literals (parser-rejection tests only),
  following `PoseSerializationTest.kt`'s existing precedent.
- Unit, integration, and system-test-equivalent coverage per the master
  plan's Phase 3 bullets.

**Out of scope:**
- **CSV parsing.** The master plan says "JSON or CSV parser" (an "or"); JSON
  is the established format — kotlinx.serialization + `PoseJson` already
  exist, and `PoseJson.kt`'s own KDoc already names "Phase 3 replay files"
  as a consumer. Adding CSV now is a second, redundant, unrequested format —
  YAGNI. Documented as a deliberate decision, same pattern Phase 2 used for
  deferring the iOS integration test.
- **Any file/disk/resource IO in `shared`.** The loader's public API is a
  JSON `String -> List<SkeletonFrame>` function only. Real import of on-disk
  recorded device sequences is deferred to Phase 12. Per the user's explicit
  decision above.
- **Real pose normalization, filtering, or movement detection logic** —
  `PoseFrameSink` is an interface only; Phases 4–7 provide real
  implementations. This phase's tests use fixed, trivial test-double sinks.
- **A CLI module or command-line entry point** — `settings.gradle.kts` has
  no CLI target; the master plan's system-test bullet explicitly accepts a
  "test-runner workflow" as an alternative, satisfied by `commonTest`
  running under both current Gradle test tasks.
- **Any change to `SkeletonFrame`/`Joint`/other Phase 2 model types** — no
  new ordering/uniqueness constraints added to `SkeletonFrame` itself;
  ordering/duplicate handling lives entirely in the new loader, per Phase
  2's explicit "deliberately left for Phase 3" framing.
- **Rejecting duplicate timestamps as an error** — deliberately kept
  (stable-sorted, not rejected); see Technical design and Risks.

**Technical design:**

`PoseSequenceParseException.kt`:
```kotlin
package com.snowboardpose.shared.pose.replay

/**
 * Thrown for any pose sequence JSON that cannot be turned into a usable,
 * ordered [SkeletonFrame] list: malformed syntax, wrong field types, a
 * missing required field (e.g. `timestampMillis`), a non-array root, or an
 * empty array. Extends [IllegalArgumentException], matching this codebase's
 * existing validate-via-`require()` convention (see
 * [com.snowboardpose.shared.pose.Joint], [com.snowboardpose.shared.pose.MovementEvent]).
 */
class PoseSequenceParseException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
```

`PoseSequenceLoader.kt`:
```kotlin
package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.PoseJson
import com.snowboardpose.shared.pose.SkeletonFrame
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

/**
 * Parses a recorded pose sequence from JSON text (format: [PoseJson]) into
 * an ordered [List] of [SkeletonFrame]. Pure function — no file/disk/network
 * IO; getting JSON text into memory is the caller's job (tests now, a
 * future Phase 12 file-import adapter later).
 */
object PoseSequenceLoader {

    fun loadJson(text: String): List<SkeletonFrame> {
        val frames = try {
            PoseJson.decodeFromString<List<SkeletonFrame>>(text)
        } catch (e: SerializationException) {
            throw PoseSequenceParseException("Malformed pose sequence JSON: ${e.message}", e)
        }
        if (frames.isEmpty()) {
            throw PoseSequenceParseException("Pose sequence must contain at least one frame")
        }
        // Stable sort: input need not be pre-sorted. Frames with equal
        // timestampMillis are NOT rejected — they keep their original
        // relative order. Real-world duplicate timestamps are a valid
        // input that downstream detectors (Phase 5) are responsible for
        // tolerating; this loader's job is deterministic ordering, not
        // policy.
        return frames.sortedBy { it.timestampMillis }
    }
}
```
`timestampMillis` has no default on `SkeletonFrame` (confirmed by direct
read), so a frame object missing that key already fails
kotlinx.serialization decoding via `MissingFieldException` (a
`SerializationException` subtype) — no bespoke validation code is needed for
the "missing timestamps are rejected" bullet, only a test proving it.

`PoseFrameSink.kt`:
```kotlin
package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.SkeletonFrame

/**
 * Placeholder processing-pipeline contract standing in for the real
 * normalization/filtering/detection pipeline Phases 4-7 build. Given the
 * current frame and the immediately preceding one (`null` on the first
 * frame), returns a [MovementEvent] if this frame produced one.
 */
fun interface PoseFrameSink {
    fun onFrame(frame: SkeletonFrame, previous: SkeletonFrame?): MovementEvent?
}
```

`RecordedSequenceReplayer.kt`:
```kotlin
package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.SkeletonFrame

/**
 * Deterministically replays an already-ordered frame sequence through a
 * [PoseFrameSink]: plain synchronous iteration, no timers, no wall-clock,
 * no coroutines — two replays of the same input always produce identical
 * output.
 */
object RecordedSequenceReplayer {

    fun replay(frames: List<SkeletonFrame>, sink: PoseFrameSink): List<MovementEvent> {
        val events = mutableListOf<MovementEvent>()
        var previous: SkeletonFrame? = null
        for (frame in frames) {
            sink.onFrame(frame, previous)?.let(events::add)
            previous = frame
        }
        return events
    }
}
```
A stateless `object` (not a constructed class) — consistent with
`PoseSequenceLoader`/`PoseJson`, and it holds no state beyond its immutable
parameters.

`PoseFixtures.kt` (`commonTest`):
```kotlin
package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.Joint
import com.snowboardpose.shared.pose.JointType
import com.snowboardpose.shared.pose.PoseJson
import com.snowboardpose.shared.pose.SkeletonFrame
import kotlinx.serialization.encodeToString

/**
 * Hand-built in-memory pose-sequence fixtures — embedded Kotlin constants,
 * per the project's chosen approach (no multiplatform resources mechanism
 * exists in this repo). [idleFixtureJson]/[movementFixtureJson] are derived
 * from [idleFrames]/[movementFrames] via [PoseJson] itself — not hand-typed
 * JSON — so they can't drift from the real wire format.
 */
object PoseFixtures {
    // ~9 frames, 33ms apart (~30fps), joints essentially static with tiny
    // jitter — a standing-still person.
    val idleFrames: List<SkeletonFrame> = ...

    // Same cadence; hip/ankle Y showing a clear rise-then-fall pattern,
    // distinguishable from idle at a glance. Not functionally interpreted
    // this phase (no detector exists yet) — a regression fixture for
    // Phases 6/7 to reuse.
    val movementFrames: List<SkeletonFrame> = ...

    val idleFixtureJson: String = PoseJson.encodeToString(idleFrames)
    val movementFixtureJson: String = PoseJson.encodeToString(movementFrames)
}
```

**Files to create:**
```
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceParseException.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceLoader.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/PoseFrameSink.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/replay/RecordedSequenceReplayer.kt

shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseFixtures.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceLoaderTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/RecordedSequenceReplayerTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/replay/PoseSequenceReplayIntegrationTest.kt
```

**Files to modify:** None required for functionality. Optional, cosmetic
only: a one-line KDoc addition to
`shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseModelFixtureIntegrationTest.kt`
noting the real loader now exists at `pose.replay.PoseSequenceLoader` —
implementer's call, not required for completion. (`Plan Files/milestone_roadmap.md`'s
status row will also be updated as part of the completion report, per
precedent — not an implementation-time file change.)

**Files to delete:** None.

**Dependencies:** None new. Reuses existing `kotlinx-serialization-json`
(1.11.0) and `kotlin-test`. No okio, no kotlinx-datetime, no CSV library, no
new Gradle module.

**Implementation steps:**
1. Create the `commonMain` package directory `pose/replay/`.
2. Create `PoseSequenceParseException.kt`.
3. Create `PoseSequenceLoader.kt` (parse, empty check, stable sort — both
   failure paths throw `PoseSequenceParseException`).
4. Create `PoseFrameSink.kt` (`fun interface`).
5. Create `RecordedSequenceReplayer.kt` (stateless `object`).
6. Create the `commonTest` package directory `pose/replay/`.
7. Create `PoseFixtures.kt` (`idleFrames`, `movementFrames`, and their
   `PoseJson`-derived JSON strings).
8. Write `PoseSequenceLoaderTest.kt`.
9. Write `RecordedSequenceReplayerTest.kt`.
10. Write `PoseSequenceReplayIntegrationTest.kt`, with KDoc explicitly
    documenting the "test-runner workflow" mapping for the master plan's
    system-test bullet (same technique Phase 2 used for its Android-adapter
    integration test).
11. (Optional) amend `PoseModelFixtureIntegrationTest.kt`'s KDoc.
12. Run `./gradlew :shared:jvmTest`; fix failures.
13. Run `./gradlew :shared:testAndroidHostTest`; fix any Android-target-only
    issues.
14. Run `./gradlew build` to confirm `androidApp` still assembles.
15. Report using the master plan's Section 9 format; stop (do not start
    Phase 4 automatically).

**Unit tests:**
- `PoseSequenceLoaderTest`:
  - `loadJson_validJsonArray_decodesAllFrames`
  - `loadJson_framesOutOfOrderInInput_returnsAscendingByTimestamp`
  - `loadJson_duplicateTimestamps_preservesStableRelativeOrder`
  - `loadJson_missingTimestampKey_throwsPoseSequenceParseException`
  - `loadJson_malformedJsonSyntax_throwsPoseSequenceParseException`
  - `loadJson_wrongFieldType_throwsPoseSequenceParseException` (e.g.
    `timestampMillis` as a string)
  - `loadJson_nonArrayRoot_throwsPoseSequenceParseException` (single object
    instead of an array)
  - `loadJson_emptyArray_throwsPoseSequenceParseException`
- `RecordedSequenceReplayerTest`:
  - `replay_deliversFramesToSinkInOrder`
  - `replay_firstFrame_previousIsNull`
  - `replay_subsequentFrames_previousIsPriorFrame`
  - `replay_collectsOnlyNonNullSinkEvents`
  - `replay_isDeterministic_repeatedRunsProduceIdenticalOutput`
  - `replay_emptyFrameList_returnsEmptyEventsWithoutInvokingSink`
  - `replay_frameCountDeliveredMatchesInputSize`

**Integration tests** (`PoseSequenceReplayIntegrationTest`, `commonTest`,
runs under both `:shared:jvmTest` and `:shared:testAndroidHostTest`):
- *Recorded file loads into `SkeletonFrame` objects*:
  `PoseSequenceLoader.loadJson(PoseFixtures.idleFixtureJson)` decodes to the
  expected frame list.
- *Loaded sequence passes through a placeholder pipeline*: replay the loaded
  sequence through a fixed test-double `PoseFrameSink`.
- *Expected number of frames reaches the detector*: assert the sink is
  invoked once per frame, count matches `PoseFixtures.idleFrames.size`.
- *Output events are captured correctly*: a fixed sink (e.g., emits one
  `MovementEvent(NONE, ..., 1f)` on a specific known frame index, `null`
  otherwise) replayed over `PoseFixtures.movementFrames` produces an exact,
  asserted `List<MovementEvent>`.

**System tests:**
- *Test-runner workflow processes a complete fixture* / *a known fixture
  produces a known output summary*: satisfied by
  `PoseSequenceReplayIntegrationTest` running identically under
  `:shared:jvmTest` and `:shared:testAndroidHostTest` — the master plan's
  own wording accepts "test-runner workflow" as an alternative to a CLI, and
  this repo has no CLI module. Documented explicitly in the test's KDoc,
  following the same mapping technique Phase 2 used for its Android-adapter
  integration test, so a future reader doesn't expect a separate CLI entry
  point.

**Performance tests:** None required this phase — no per-frame heavy
computation exists yet (`PoseFrameSink` implementations under test are
trivial). Becomes meaningful once Phase 5's temporal buffer and Phase 6/7
detectors run over larger recorded sequences, same framing Phase 2 used for
deferring performance tests.

**Validation commands:**
```
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew build
```

**Risks:**
- Real, on-disk, device-recorded MediaPipe JSON is never exercised this
  phase — only hand-built fixtures in the same shape as `PoseJson`
  produces. If a real recording has a shape `PoseJson`/`SkeletonFrame`
  doesn't tolerate, that surfaces in Phase 12 (first phase with real file
  import), not now.
- `catch (e: SerializationException)` is verified (by decompiling
  `kotlinx-serialization-json-jvm`/`-core-jvm` 1.11.0 classes from the
  Gradle cache) to cover `JsonDecodingException` (malformed syntax) and
  `MissingFieldException` (missing required field) today. A future
  kotlinx.serialization upgrade could in principle introduce a
  malformed-input failure mode outside this hierarchy; low probability,
  mitigated by the multi-shape malformed-input test list above, but not
  exhaustively provable.
- Duplicate-timestamp handling (stable-sort, not rejected) is a deliberate
  policy choice that Phase 5's plan must be written with awareness of — it
  must not assume duplicates were already filtered out upstream.
- Kotlin's `sortedBy` stability is confirmed for both current targets
  (`android`, `jvm` — both JVM-backed, stable `Arrays.sort`/TimSort for
  objects). Not yet proven under a Kotlin/Native target since none exists
  in this repo (Milestone 4's concern).
- `pose.replay` is the first subpackage under `pose/`, a deliberate
  departure from Phase 2's flat layout (see Technical design) — flagged so
  it's read as an intentional precedent, not an inconsistency.

**Completion criteria:**
- All new files compile for both `android` and `jvm` targets
  (`./gradlew build` succeeds).
- All unit/integration tests pass under both `:shared:jvmTest` and
  `:shared:testAndroidHostTest`.
- At least one idle fixture and one synthetic movement fixture exist
  (`PoseFixtures.idleFrames`/`movementFrames`) and are exercised both
  directly and via their `PoseJson`-encoded JSON strings.
- `PoseSequenceLoader` deterministically orders frames and uniformly
  rejects malformed input (empty array, missing required field, malformed
  syntax, wrong field type) via `PoseSequenceParseException`.
- `RecordedSequenceReplayer.replay` is proven deterministic (two runs of
  the same input produce identical output) and requires no camera device.
- Harness runs via the existing `scripts/test.sh` / normal Gradle test
  command — no new module, no new dependency.
- No platform-specific types or file/disk/network IO added to
  `commonMain`/`commonTest` (verified by inspection).
- CSV parsing and real file-based import are explicitly recorded as
  out-of-scope/deferred (Phase 12), not silently missing.

---

## Verification (once Phase 3 is implemented)

1. `./gradlew :shared:jvmTest` — all new loader/replayer/integration tests
   pass.
2. `./gradlew :shared:testAndroidHostTest` — same tests pass under the
   Android target.
3. `./gradlew build` — full project (including `androidApp`) still
   compiles.
4. Report results using the master plan's Section 9 format (created/
   modified files, dependencies added, test results, build results, known
   limitations, recommended next phase — Phase 4: Pose Validation and
   Normalization, per Milestone 2).

No implementation happens until this Phase 3 plan is explicitly approved,
per the master plan's process. This plan should also be copied into
`Plan Files/` (e.g. `milestone_2_phase_3_plan.md`) at implementation time,
per the project's standing rule that all plans live in that directory, not
only in the agent's private plan-mode scratch file.
