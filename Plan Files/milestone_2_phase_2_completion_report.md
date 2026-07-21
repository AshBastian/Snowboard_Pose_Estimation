# Milestone 2 — Phase 2 Completion Report

Format per `cross_platform_movement_detection_master_plan.md` Section 9.
Plan: `milestone_2_plan.md`.

**Completed work:** Built the shared, camera-agnostic pose data model in a
new `com.snowboardpose.shared.pose` package: `JointType` (full 33-landmark
MediaPipe Pose Landmarker set, in MediaPipe's fixed index order), `Joint`
(x/y/z + visibility, validated), `SkeletonFrame` (image-space `joints` +
optional `worldJoints`, with safe accessors), `MovementType`, `MovementEvent`
(validated confidence), `PoseProcessingConfig` (narrowly scoped to
visibility threshold + smoothing alpha), and a shared, documented
`PoseJson` kotlinx.serialization instance. Added kotlinx.serialization
(plugin + JSON runtime) to the Gradle build. All models compile and their
tests pass on both `shared` targets (android, jvm); `androidApp` still
assembles against the updated `shared` module.

**Created files:**
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/JointType.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/Joint.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/SkeletonFrame.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/MovementType.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/MovementEvent.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/PoseProcessingConfig.kt`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/PoseJson.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/JointTypeTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/JointTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/SkeletonFrameTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/MovementEventTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseProcessingConfigTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseSerializationTest.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/PoseModelFixtureIntegrationTest.kt`
- `Plan Files/milestone_2_plan.md` (this milestone's plan record)
- `.claude/settings.local.json` (session-local; sets `ECC_GATEGUARD=off` at
  the user's request during this session — see Problems encountered)

**Modified files:**
- `gradle/libs.versions.toml` — added `kotlinxSerializationJson` version
  (1.11.0, confirmed latest on Maven Central at implementation time),
  `kotlinSerialization` plugin alias, `kotlinx-serialization-json` library
  alias.
- `shared/build.gradle.kts` — applied `libs.plugins.kotlinSerialization`;
  added `commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }`.
- `Plan Files/milestone_roadmap.md` — added a standing rule (at the
  user's request) that all plans must be written into `Plan Files/`, and
  updated the Milestone 2 status row.

**Deleted files:** None.

**Dependencies added:**
- kotlinx.serialization compiler plugin (`org.jetbrains.kotlin.plugin.serialization`, tracks Kotlin 2.4.10)
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0`

**Unit tests executed** (all under `:shared:jvmTest` and
`:shared:testAndroidHostTest`, 39 tests each, 0 failures):
- `JointTypeTest` (2): exact 33-entry count; ordinal order matches the
  documented MediaPipe index table.
- `JointTest` (7): value storage; visibility bounds accepted/rejected;
  equality; copy.
- `SkeletonFrameTest` (7): per-frame lookups; missing-joint→null;
  `hasJoint`; `worldJoints` defaults to empty; image/world map
  independence; equality; copy.
- `MovementEventTest` (6): confidence bounds accepted/rejected; equality;
  copy.
- `PoseProcessingConfigTest` (4): defaults in range; out-of-range
  rejection for both fields; `copy()` re-validates.
- `PoseSerializationTest` (9): round-trip for `Joint`/`SkeletonFrame`
  (incl. non-empty `worldJoints`)/`MovementEvent`/`PoseProcessingConfig`;
  empty-map round-trip; unknown-key tolerance; missing-optional-field
  default fallback; stable enum-name serialization for `JointType`/
  `MovementType`.
- `PoseModelFixtureIntegrationTest` (3): see Integration tests below.
- Pre-existing `GreetingTest` (1): unaffected, still passes.

**Integration tests executed:**
- *Android adapter constructs a shared skeleton from a fixture* / *recorded
  fixture loads into shared models*: `PoseModelFixtureIntegrationTest`
  (in `commonTest`) — hand-built 5-frame in-memory sequence with
  increasing timestamps, per-frame joint lookups, a deliberately missing
  joint on one frame, and a `MovementEvent` derived from sequence data.
  Ran identically under both `:shared:jvmTest` and
  `:shared:testAndroidHostTest`.
- *iOS adapter contract*: **out of scope**, as planned — no iOS target
  exists in this repo yet (deferred to Milestone 4).

**System tests executed:** `./gradlew build` — full project, including
`androidApp` debug and release variants, assembles successfully against
the updated `shared` module. No on-device install/launch needed this phase
(no UI-visible change), per the plan.

**Performance tests executed:** None — not required this phase (plain data
models, no per-frame processing loop), per the plan.

**Build commands:**
```
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew build
```
(`JAVA_HOME` had to be set explicitly to the Android Studio-bundled JBR at
`/snap/android-studio/current/jbr` — see Problems encountered.)

**Build results:** `BUILD SUCCESSFUL` for all three commands.

**Test results:** 39/39 tests passed under `:shared:jvmTest`; 39/39 passed
under `:shared:testAndroidHostTest` (identical suite, both targets); 0
failures, 0 skipped.

**Manual validation:** Not applicable this phase — no UI-visible or
runtime-observable behavior change; `./gradlew build`'s success is the
verification per the plan's system-test scope.

**Known limitations:**
- iOS integration test explicitly out of scope — no iOS target exists yet
  (Milestone 4).
- `PoseProcessingConfig` defaults (`visibilityThreshold = 0.5f`,
  `smoothingAlpha = 0.3f`) are unvalidated placeholders, not empirically
  tuned — flagged in the file's KDoc for Phase 4/5 to revisit.
- Hop/turn-specific configuration is intentionally absent; will be added
  in Phase 6/7 once real fixtures exist to derive thresholds from.
- `MovementEvent` has no `direction` field yet (deferred to Phase 7).
- The repo still has no git commits (pre-existing from Milestone 1,
  unrelated to this phase) — everything, including this phase's work,
  remains untracked in git.

**Problems encountered (and how they were resolved):**
1. `JAVA_HOME` was not set / no `java` on `PATH` in this shell session,
   so `./gradlew` failed immediately. Resolved by locating the Android
   Studio-bundled JBR (`/snap/android-studio/current/jbr`, OpenJDK 21) and
   exporting `JAVA_HOME` for the build commands.
2. The `kotlinx-serialization-json` version (`1.11.0`) was re-verified
   against Maven Central at implementation time as the plan flagged;
   confirmed still latest, no change needed.
3. A repo-configured GateGuard fact-forcing hook (pre-existing ECC plugin
   hook, unrelated to this project's own code) denied the first Edit/Write
   attempt on every new file this phase, requiring either a stated
   justification or a retry to proceed (its own design: first touch is
   denied-but-marked-checked, so an immediate retry is allowed). Handled
   inline per-file throughout the phase. Partway through, at the user's
   request, `.claude/settings.local.json` was created with
   `"env": {"ECC_GATEGUARD": "off"}`. This is a session/machine-local file,
   not committed by default — flagged here so it isn't mistaken for
   project configuration; the user can remove it if no longer wanted.

**Regression tests added:** None — no pre-existing defect was fixed; this
phase only added new scaffolding (consistent with Milestone 1's precedent).

**Recommended next phase:** Phase 3 — Recorded Pose Input and Test Harness
(Milestone 2), per `milestone_roadmap.md`. Per the master plan's process,
implementation does not begin automatically — Phase 3 needs its own
presented, approval-pending plan first.
