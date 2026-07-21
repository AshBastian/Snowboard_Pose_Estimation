# Milestone 2 — Shared Movement Detection Core (overview + Phase 2 plan)

## Context

`milestone_roadmap.md` groups the master plan's 21 phases into 6 milestones.
Milestone 1 (Phases 0–1) is complete: a real, buildable KMP repo exists — a
`shared` module (android + jvm targets, no iOS yet — deferred to Milestone 4)
and an `androidApp` module — proven end-to-end with a placeholder
`sharedGreeting()` value rendered on a physical device.

Milestone 2 (Phases 2–7) is next: build all movement-detection logic — pose
model, recorded-fixture test harness, normalization, filtering, hop
detector, turn detector — entirely inside `shared`, with zero camera/platform
dependency, fully unit/integration tested on desktop/CI.

Per the master plan's process (unchanged for every phase, including inside a
milestone): each phase is individually inspected, planned, presented, and
approved **before** any files change, implemented only once approved, then
tested and reported on — and the agent stops before starting the next phase
automatically. A milestone is a grouping for tracking only; it does not
bundle phases into one approval. **This document therefore only carries a
full, approval-pending plan for Phase 2** (the first phase of the
milestone). Phases 3–7 will each get their own detailed plan, presented and
approved individually, once Phase 2 is implemented and reported on — the
milestone-level goal/exit-demo above is the throughline connecting them, not
a single up-front implementation blob.

Two design decisions were confirmed with the user before finalizing the
Phase 2 plan below:
- **Joint set:** the shared `JointType` enum uses MediaPipe Pose Landmarker's
  full 33-landmark set (not the master plan's smaller 17-joint illustrative
  example). Rationale: an exact 1:1 index mapping in Phase 9/15 instead of a
  lossy subset mapping, and real eye/ear joints for Phase 7's face-visibility
  orientation classification, which the 17-joint set can't provide (it only
  has `NOSE`). Later detectors are free to ignore joints they don't need.
- **Validation strategy:** invalid `visibility`/`confidence` values (outside
  `[0,1]`) are **rejected via `require()`** (throws `IllegalArgumentException`)
  rather than silently clamped. Real MediaPipe visibility is always in
  `[0,1]` by construction, so this only ever fires on a malformed fixture or
  a genuine upstream bug — fail-fast surfaces that immediately. Clamping was
  rejected because it cannot be made to survive `copy()`/deserialization
  intact on a plain Kotlin `data class` without extra machinery.

---

## Current repo state (verified directly, relevant to Phase 2)

- `shared/src/commonMain/kotlin/com/snowboardpose/shared/Greeting.kt` —
  only `fun sharedGreeting(): String = "..."`.
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/GreetingTest.kt` —
  one trivial `kotlin.test` assertion.
- `shared/src/androidMain/kotlin/com/snowboardpose/shared/` exists as an
  empty directory. No `iosMain`, no `resources`, no fixture/JSON/CSV files
  anywhere in the repo.
- `shared/build.gradle.kts`: KMP plugin + the new AGP9
  `com.android.kotlin.multiplatform.library` plugin; `android { ... }` +
  `jvm()` targets only; `commonTest.dependencies { implementation(libs.kotlin.test) }`;
  no `commonMain.dependencies` block yet; no serialization plugin/library.
- `gradle/libs.versions.toml`: agp 9.3.0, kotlin 2.4.10, composeBom,
  androidx activity/appcompat, SDK versions, plugin aliases for
  androidApplication/androidKotlinMultiplatformLibrary/kotlinMultiplatform/
  composeCompiler, and one library alias `kotlin-test`. No serialization or
  datetime entries.
- `settings.gradle.kts` includes only `:shared` and `:androidApp`.
- Established validation commands (from `milestone_1_completion_report.md`):
  `./gradlew build`, `./gradlew :shared:jvmTest`,
  `./gradlew :shared:testAndroidHostTest`,
  `./gradlew :androidApp:installDebug`. `commonTest` sources run under both
  `:shared:jvmTest` and `:shared:testAndroidHostTest` automatically.
- **Note (not part of this phase, flagged for awareness):** the repo has no
  git commits yet — everything from Milestone 1 is still untracked. Worth
  committing before or alongside Phase 2, but that's the user's call, not
  bundled into this plan.

---

## Phase 2 plan — Shared Pose Data Model

**Phase:** 2 (Milestone 2, first of six)

**Objective:** Create shared, camera-agnostic Kotlin data models for
MediaPipe-style pose joints, per-frame skeletons (image-space + optional
world-space), movement events, and narrowly-scoped processing
configuration — with a documented, stable JSON serialization format —
compiling cleanly on both `shared` targets (android, jvm).

**Current state:** See above — `shared` currently contains only the
placeholder `sharedGreeting()` function and its test; nothing pose-related
exists yet.

**Scope:**
- New package `com.snowboardpose.shared.pose`, `commonMain`/`commonTest`
  only.
- `JointType` (33-entry MediaPipe set), `Joint`, `SkeletonFrame`
  (image-space `joints` + optional `worldJoints`), `MovementType`,
  `MovementEvent`, `PoseProcessingConfig` (visibility threshold +
  smoothing alpha only).
- Add kotlinx.serialization (plugin + JSON runtime) to the Gradle build,
  plus one shared, documented `Json` instance.
- Model-level construction-time validation (visibility/confidence bounds
  via `require()`).
- Unit tests, serialization round-trip tests, one hand-built in-memory
  fixture integration test.

**Out of scope:**
- iOS adapter contract test — no iOS target exists in this repo yet
  (deferred to Milestone 4 per the Milestone 1 decision). Explicitly
  scoped out, not silently skipped.
- File-based fixture loading (JSON/CSV parsing, replay harness) — Phase 3.
- Pose normalization (hip/shoulder center, torso length, mirroring,
  visibility thresholding as an algorithm) — Phase 4.
- Camera orientation / front-camera mirroring metadata on the model —
  platform adapters (Phase 8/9, 14/15) normalize orientation before a
  `SkeletonFrame` is ever constructed, so shared code doesn't need these
  fields. Deliberate boundary, not an oversight.
- Hop/turn-specific thresholds in `PoseProcessingConfig` — deferred to
  Phase 6/7, once there's empirical basis for real numbers.
- `MovementEvent.direction` (clockwise/counterclockwise) — deferred to
  Phase 7 (YAGNI; trivial additive change later).
- Any real MediaPipe/platform adapter code. The "Android adapter
  constructs a shared skeleton" integration test is satisfied by running
  the same `commonTest` fixture test under the Android target (see
  Integration tests below), not by writing `androidMain` code.

**Technical design:**

`JointType` — `@Serializable enum class` with all 33 MediaPipe Pose
Landmarker landmarks in MediaPipe's fixed index order (`NOSE` … 1
`LEFT_EYE_INNER` … 7 `LEFT_EAR` … 11 `LEFT_SHOULDER` … 17 `LEFT_PINKY` … 23
`LEFT_HIP` … 27 `LEFT_ANKLE` … 32 `RIGHT_FOOT_INDEX`). Serialized by enum
name; ordinal order is locked by a guard test since it encodes the 1:1
MediaPipe index mapping later phases rely on.

`Joint`:
```kotlin
@Serializable
data class Joint(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float,
) {
    init {
        require(visibility in 0f..1f) { "visibility must be in [0,1], was $visibility" }
    }
}
```
`x`/`y` are normalized image coordinates when stored in
`SkeletonFrame.joints`, metric/hip-centered when stored in
`SkeletonFrame.worldJoints` — same type, meaning supplied by which map
holds it.

`SkeletonFrame`:
```kotlin
@Serializable
data class SkeletonFrame(
    val timestampMillis: Long,
    val joints: Map<JointType, Joint> = emptyMap(),
    val worldJoints: Map<JointType, Joint> = emptyMap(),
) {
    fun joint(type: JointType): Joint? = joints[type]
    fun worldJoint(type: JointType): Joint? = worldJoints[type]
    fun hasJoint(type: JointType): Boolean = joints.containsKey(type)
}
```
Missing joints = absent map entries, never null fields. `worldJoints`
defaults to `emptyMap()` (not nullable) — "no world data" and "zero world
joints" are the same state.

`MovementType` — `@Serializable enum class { NONE, HOP, TURN_AROUND }`.

`MovementEvent`:
```kotlin
@Serializable
data class MovementEvent(
    val type: MovementType,
    val timestampMillis: Long,
    val confidence: Float,
) {
    init {
        require(confidence in 0f..1f) { "confidence must be in [0,1], was $confidence" }
    }
}
```

`PoseProcessingConfig` (deliberately narrow — see Out of scope):
```kotlin
@Serializable
data class PoseProcessingConfig(
    val visibilityThreshold: Float = 0.5f,
    val smoothingAlpha: Float = 0.3f,
) {
    init {
        require(visibilityThreshold in 0f..1f) { "visibilityThreshold must be in [0,1], was $visibilityThreshold" }
        require(smoothingAlpha in 0f..1f) { "smoothingAlpha must be in [0,1], was $smoothingAlpha" }
    }
}
```
Defaults are documented as unvalidated placeholders for Phase 4/5 to
consume/override, not tuned values.

`PoseJson` — one shared, documented instance:
```kotlin
val PoseJson: Json = Json {
    ignoreUnknownKeys = true   // forward-compatible with fields later phases add
    encodeDefaults = true      // recorded fixtures are self-describing
}
```

**Files to create:**
```
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/JointType.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/Joint.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/SkeletonFrame.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/MovementType.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/MovementEvent.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/PoseProcessingConfig.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/PoseJson.kt

shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/JointTypeTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/JointTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/SkeletonFrameTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/MovementEventTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseProcessingConfigTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseSerializationTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseModelFixtureIntegrationTest.kt
```

**Files to modify:** `gradle/libs.versions.toml`, `shared/build.gradle.kts`.

**Files to delete:** None.

**Dependencies:**
`gradle/libs.versions.toml` additions:
```toml
[versions]
kotlinxSerializationJson = "1.11.0"   # re-verify latest at implementation time

[plugins]
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```
(The serialization *compiler plugin* tracks the Kotlin version directly,
same pattern as `composeCompiler`; only the runtime library has its own
version.)

`shared/build.gradle.kts` additions: apply `libs.plugins.kotlinSerialization`;
add `commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }`
alongside the existing `commonTest.dependencies` block.

**Implementation steps:**
1. Add version/plugin/library entries to `gradle/libs.versions.toml`.
2. Apply the plugin + add `commonMain.dependencies` in `shared/build.gradle.kts`.
3. Create `JointType.kt` (33 entries, MediaPipe index order).
4. Create `Joint.kt` with `require()`-based visibility validation.
5. Create `SkeletonFrame.kt` with `joints`/`worldJoints` + accessors.
6. Create `MovementType.kt`.
7. Create `MovementEvent.kt` with `require()`-based confidence validation.
8. Create `PoseProcessingConfig.kt`.
9. Create `PoseJson.kt` with format-guarantee KDoc.
10. Write `JointTypeTest.kt`, `JointTest.kt`, `SkeletonFrameTest.kt`,
    `MovementEventTest.kt`, `PoseProcessingConfigTest.kt`.
11. Write `PoseSerializationTest.kt` (round trips + unknown/missing fields).
12. Write `PoseModelFixtureIntegrationTest.kt` (hand-built in-memory fixture).
13. Run `./gradlew :shared:jvmTest`; fix failures.
14. Run `./gradlew :shared:testAndroidHostTest`; fix any Android-target issues.
15. Run `./gradlew build` to confirm `androidApp` still assembles.
16. Report using the master plan's Section 9 completion-report format; stop
    (do not start Phase 3 automatically).

**Unit tests:**
- `JointTypeTest`: exactly 33 entries; ordinal order matches the documented
  MediaPipe landmark index table (guards the 1:1 mapping promise).
- `JointTest`: values stored correctly; visibility `0f`/`1f` accepted at
  bounds; `<0` and `>1` throw `IllegalArgumentException`; equality; `copy()`
  changes only the targeted field.
- `SkeletonFrameTest`: `joint(type)` returns the stored joint or `null` when
  absent (missing-joint handling); `hasJoint` false for absent type;
  `worldJoints` defaults to empty; image/world maps independent;
  equality/copy correct.
- `MovementEventTest`: confidence `0f`/`1f` accepted; `<0`/`>1` throw;
  equality/copy correct.
- `PoseProcessingConfigTest`: defaults within `[0,1]`; out-of-range
  `visibilityThreshold`/`smoothingAlpha` throw; `copy()` re-validates
  (validation is `init`-based, not bypassable).
- `PoseSerializationTest`: round-trip preserves all fields for `Joint`,
  `SkeletonFrame` (including non-empty `worldJoints`), `MovementEvent`,
  `PoseProcessingConfig`; round-trip preserves empty `joints`/`worldJoints`;
  unknown JSON key doesn't throw (`ignoreUnknownKeys`); missing optional
  field falls back to its default; `JointType`/`MovementType` serialize by
  stable name across all entries.

**Integration tests:**
- *Android adapter constructs a shared skeleton model from a fixture*:
  satisfied by `PoseModelFixtureIntegrationTest` (in `commonTest`) running
  under `./gradlew :shared:testAndroidHostTest` — per Milestone 1 precedent,
  `commonTest` runs for the Android target automatically. Documented in the
  test's KDoc so a future reader doesn't expect a separate `androidMain`
  test file.
- *iOS adapter contract constructs the same logical skeleton*: **out of
  scope** — no iOS target exists yet (Milestone 4).
- *Recorded fixture loads into shared models*: satisfied by the same test,
  using a hand-built in-memory sequence (3–5 `SkeletonFrame`s with changing
  hip/ankle joints, one frame with a deliberately missing joint) asserting
  frame ordering, correct per-frame lookups, safe handling of the missing
  joint, and that a `MovementEvent` can be constructed from data derived
  from the sequence.

**System tests:** Per the master plan's own Phase 2 criteria (not required
beyond confirming both apps still build): `./gradlew build` succeeds and
`androidApp` still assembles against the updated `shared` module. No
on-device install/launch needed — no UI-visible change this phase.

**Performance tests:** None required — plain data models/config with O(1)
construction, no per-frame processing loop yet. Becomes meaningful from
Phase 5 (temporal buffer) onward.

**Validation commands:**
```
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew build
```

**Risks:**
- `kotlinx-serialization-json` pinned to `1.11.0` based on a Maven Central
  check at planning time — re-verify latest/compatible version at
  implementation time.
- The AGP9 `com.android.kotlin.multiplatform.library` plugin is very new
  (per Milestone 1's report); confirm `commonMain.dependencies` +
  `kotlinSerialization` actually triggers the `@Serializable` compiler
  plugin for the android target too, not just jvm — a silent no-op would
  only surface as a runtime `SerializationException`, not a compile error.
- `Map<JointType, Joint>` serializes as a JSON object keyed by enum name;
  document that JSON object key iteration order is not a format guarantee.
- `PoseProcessingConfig` defaults (`0.5f`, `0.3f`) are explicit placeholders,
  not empirically derived — flagged so Phase 4/5 doesn't mistake them for
  tuned values.

**Completion criteria:**
- All new files compile for both `android` and `jvm` targets
  (`./gradlew build` succeeds).
- All unit tests pass under both `:shared:jvmTest` and
  `:shared:testAndroidHostTest`.
- Serialization format is stable (round-trip + unknown/missing-field tests
  pass) and documented in `PoseJson.kt` KDoc.
- No platform-specific types appear in `commonMain`/`commonTest` (verified
  by inspection — zero `androidMain`/`iosMain` additions this phase).
- iOS integration test explicitly recorded as scoped-out (Milestone 4), not
  silently missing.

---

## Verification (once Phase 2 is implemented)

1. `./gradlew :shared:jvmTest` — all new pose-model unit/serialization/
   integration tests pass.
2. `./gradlew :shared:testAndroidHostTest` — same tests pass under the
   Android target.
3. `./gradlew build` — full project (including `androidApp`) still compiles.
4. Report results using the master plan's Section 9 format (created/modified
   files, dependencies added, test results, build results, known
   limitations, recommended next phase — Phase 3: Recorded Pose Input and
   Test Harness, per Milestone 2).

No implementation happens until this Phase 2 plan is explicitly approved,
per the master plan's process. Phases 3–7 of Milestone 2 will each get their
own presented, approval-pending plan afterward — not implemented as part of
this approval.
