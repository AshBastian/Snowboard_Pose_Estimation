# Phase 4 — Pose Validation and Normalization

## Context

This plan was written one session ahead of implementation, at the user's
request ("write the plan for the next phase and put it into the plan
folder for the next session"). It is **not yet approved for
implementation** — per the master plan's process, a future session must
still present this plan (or a revised version of it) and get explicit
approval before touching any files. It is written to the same level of
concreteness as an approval-pending plan would be, so that a future
session can pick it up cold.

`Plan Files/milestone_roadmap.md` groups the master plan's 21 phases into
6 milestones. Milestone 2 (Phases 2–7) builds all movement-detection logic
inside the `shared` KMP module, with zero camera/platform dependency,
fully testable on desktop/CI. Phases 2 (shared pose data model) and 3
(recorded pose input / replay test harness) are implemented and reported
on (see `Plan Files/milestone_2_plan.md`, `milestone_2_phase_3_plan.md`,
and their completion reports). Phase 4 is next per the roadmap. Its
master-plan spec lives in
`Plan Files/cross_platform_movement_detection_master_plan.md` lines
594–639 (duplicated in `milestone_roadmap.md` lines 128–146): prepare raw
skeleton data for reliable movement recognition — visibility thresholding,
missing-joint handling, hip/shoulder center calculation, torso-length
calculation, body-relative normalization, coordinate-system
normalization, and mirroring normalization where needed.

---

## Current repo state (verified directly this session)

- `shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/`:
  - `JointType.kt` — 33-entry MediaPipe-ordinal `@Serializable enum class`.
    `NOSE` is unpaired; the other 32 entries form 16 anatomical LEFT/RIGHT
    pairs (eyes ×3, ears, mouth, shoulder, elbow, wrist, pinky, index,
    thumb, hip, knee, ankle, heel, foot_index).
  - `Joint.kt` — `x`/`y`/`z`/`visibility`, `visibility` validated `[0,1]`.
  - `SkeletonFrame.kt` — `timestampMillis: Long`,
    `joints`/`worldJoints: Map<JointType, Joint> = emptyMap()`. `joints`
    holds normalized image-space landmarks; `worldJoints` holds optional
    metric, hip-centered world-space landmarks (empty when unavailable —
    no fixture populates it yet). Missing joint = absent map entry, never
    a null field.
  - `PoseProcessingConfig.kt` — confirmed unchanged this session (see
    below): `visibilityThreshold: Float = 0.5f`,
    `smoothingAlpha: Float = 0.3f`, both validated `[0,1]`. KDoc already
    names this as consumed by "Phase 4 (normalization) and Phase 5
    (filtering)" — no new config type is needed, just consume the
    existing `visibilityThreshold`.
  - `pose/replay/` (Phase 3) — `PoseSequenceLoader`, `PoseFrameSink`,
    `RecordedSequenceReplayer`, plus `commonTest`'s `PoseFixtures` (idle +
    synthetic-movement `List<SkeletonFrame>`, ~9 frames each, using
    `LEFT/RIGHT_SHOULDER`, `LEFT/RIGHT_HIP`, `LEFT/RIGHT_ANKLE` at fixed
    x=0.45/0.55, varying y, visibility 0.9–0.95). `PoseFrameSink`'s KDoc
    explicitly says it's a placeholder standing in for the real Phase 4-7
    pipeline — Phase 4 does **not** need to wire into the replay pipeline;
    it only needs to exist as a standalone, independently-testable
    pure-function module. `pose.replay` established the
    `pose.<concern>` subpackage precedent (first departure from Phase 2's
    flat layout) — Phase 4 follows the same convention with `pose.normalize`.
- Confirmed via repo-wide grep (this session): **zero existing
  normalization code anywhere** ("normaliz", "torso", "hipCenter",
  "shoulderCenter", "mirror", "Midpoint" — no matches as identifiers,
  only doc comments referencing this as future Phase 4 work).
  `androidApp/` has zero "diagnostic" screen/view/activity — confirmed
  fully greenfield, consistent with Milestone 2 being shared-module-only.
- Established architecture decisions from Phase 2/3 that constrain this
  phase:
  - Phase 2 explicitly deferred **all** platform-specific coordinate-axis
    and front-camera-mirroring-metadata handling to platform adapters
    (Phase 8/9, 14/15) — adapters normalize orientation *before* a
    `SkeletonFrame` is ever constructed. Shared code (including this
    phase's normalizer) must contain **no** platform-specific
    axis-conversion branches — only platform-agnostic relative geometry.
  - Phase 3 established the "map a master-plan system-test bullet onto
    existing `commonTest` infrastructure with an explicit KDoc note"
    technique, since there is no UI/CLI to run a literal "diagnostic app"
    or on-device system test against yet.
- Test conventions (flat per package, one `<Type>Test.kt` per production
  file, pure `kotlin.test`, descriptive `subject_condition_expectedBehavior`
  test names, no Kotest/JUnit-specific APIs/mocking) — same as Phases 2/3.
- `scripts/test.sh` runs
  `./gradlew :shared:jvmTest :shared:testAndroidHostTest --console=plain`
  — the established validation command.
- Git: one commit exists (`gitignore`); all Phase 0-3 work is still
  uncommitted/untracked (unchanged from Phase 3's report).

---

## Phase 4 plan — Pose Validation and Normalization

**Phase:** 4 (Milestone 2, third of six)

**Objective:** Convert raw `SkeletonFrame` image-space joints into
body-relative, translation- and scale-invariant coordinates, so that the
same pose produces comparable output regardless of the person's position
or distance from the camera — the prerequisite for reliable movement
recognition in Phases 5–7.

**Current state:** See above — only Phases 2/3's data model and replay
harness exist; no normalization code anywhere.

**Scope:**
- New package `com.snowboardpose.shared.pose.normalize`, `commonMain`/
  `commonTest` only, following the `pose.replay` subpackaging precedent.
- `midpoint(a, b)` / `distance2D(a, b)` — small, independently
  unit-testable pure geometry primitives (the master plan lists "Midpoint
  calculations" and "Distance calculations" as their own unit-test
  bullets, implying separable units, not inlined logic).
- `JointType.mirrored()` — an exhaustive `when` (no `else`, so the
  compiler forces an update if `JointType` ever changes) mapping each of
  the 33 joints to its anatomical mirror; `NOSE` maps to itself.
- `NormalizedPose` — the output value type: hip-relative, torso-scaled
  joints, plus the raw (pre-normalization) `hipCenter`/`shoulderCenter`/
  `torsoLength` for diagnostics and reuse by later phases (e.g. Phase 6's
  hip-vertical-displacement feature).
- `PoseNormalizer.normalize(frame, config, mirrored)` — the main entry
  point: visibility-thresholds joints, requires the 4 anchor joints
  (hips + shoulders), computes hip/shoulder centers and torso length,
  transforms every thresholded joint into hip-relative/torso-scaled
  coordinates, optionally mirrors, returns `null` if normalization isn't
  possible for this frame (not an exception — see Technical design).
- Unit, integration, and system-test-equivalent coverage per the master
  plan's Phase 4 bullets, reusing Phase 3's `pose.replay.PoseFixtures`
  where sensible rather than duplicating fixture data.

**Out of scope:**
- **Any platform-specific coordinate-axis or orientation-conversion
  logic.** Per Phase 2's explicit decision, that lives entirely in
  platform adapters (Phase 8/9, 14/15), before a `SkeletonFrame` is ever
  constructed. "Coordinate-system normalization" in this phase means the
  hip-relative/torso-scaled transform itself — not a separate axis-flip
  step. See Technical design for how the master plan's "coordinate-axis
  conversion" test bullet is satisfied without violating this boundary.
- **Wiring the normalizer into `RecordedSequenceReplayer`/`PoseFrameSink`.**
  It stays a standalone, independently-callable pure function this phase.
  Combining it with real detectors into the actual pipeline is Phase 5+'s
  job (filtering comes next; detectors consume the combined
  normalized+filtered stream).
- **Joint filtering / smoothing / temporal buffering** (`smoothingAlpha`,
  velocity, jitter reduction) — that's Phase 5, unchanged from the
  existing `PoseProcessingConfig` KDoc's phase split.
- **Any change to `PoseProcessingConfig`, `SkeletonFrame`, `Joint`, or
  other Phase 2 model types.** `visibilityThreshold` already exists and
  is consumed as-is.
- **A real on-device "diagnostic application" UI.** `androidApp/` stays
  untouched this phase (Milestone 2 is shared-module-only, zero camera/UI
  dependency, per the milestone's own stated goal). The master plan's
  system-test bullet is mapped onto `commonTest` — see System tests below.
- **Depth (`z`) in torso-length/distance calculations.** `distance2D` uses
  only `x`/`y`. Phase 2/3 fixtures always carry `z = 0f` (image-space
  joints don't have reliable depth; only the separate, currently-unused
  `worldJoints` map would), so a 3D calculation would divide by a value
  that's identical to the 2D case today anyway, while prematurely coupling
  normalization to depth data that doesn't reliably exist. Revisit if a
  later phase starts populating `worldJoints` with real depth.

**Technical design:**

`PoseGeometry.kt`:
```kotlin
package com.snowboardpose.shared.pose.normalize

import com.snowboardpose.shared.pose.Joint
import kotlin.math.sqrt

/**
 * Midpoint of two joints. Visibility is the minimum of the two inputs —
 * a derived point's confidence cannot exceed its weaker source joint.
 */
fun midpoint(a: Joint, b: Joint): Joint = Joint(
    x = (a.x + b.x) / 2f,
    y = (a.y + b.y) / 2f,
    z = (a.z + b.z) / 2f,
    visibility = minOf(a.visibility, b.visibility),
)

/**
 * 2D (x, y only) Euclidean distance between two joints. Depth (z) is
 * excluded deliberately — see this phase's plan, "Out of scope".
 */
fun distance2D(a: Joint, b: Joint): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}
```

`JointMirroring.kt`:
```kotlin
package com.snowboardpose.shared.pose.normalize

import com.snowboardpose.shared.pose.JointType
import com.snowboardpose.shared.pose.JointType.*

/**
 * This joint's anatomical mirror (LEFT <-> RIGHT for the 16 paired
 * joints; [NOSE] maps to itself). Exhaustive, no `else` branch, so
 * adding a JointType entry forces this to be updated too.
 */
fun JointType.mirrored(): JointType = when (this) {
    NOSE -> NOSE
    LEFT_EYE_INNER -> RIGHT_EYE_INNER
    RIGHT_EYE_INNER -> LEFT_EYE_INNER
    LEFT_EYE -> RIGHT_EYE
    RIGHT_EYE -> LEFT_EYE
    LEFT_EYE_OUTER -> RIGHT_EYE_OUTER
    RIGHT_EYE_OUTER -> LEFT_EYE_OUTER
    LEFT_EAR -> RIGHT_EAR
    RIGHT_EAR -> LEFT_EAR
    MOUTH_LEFT -> MOUTH_RIGHT
    MOUTH_RIGHT -> MOUTH_LEFT
    LEFT_SHOULDER -> RIGHT_SHOULDER
    RIGHT_SHOULDER -> LEFT_SHOULDER
    LEFT_ELBOW -> RIGHT_ELBOW
    RIGHT_ELBOW -> LEFT_ELBOW
    LEFT_WRIST -> RIGHT_WRIST
    RIGHT_WRIST -> LEFT_WRIST
    LEFT_PINKY -> RIGHT_PINKY
    RIGHT_PINKY -> LEFT_PINKY
    LEFT_INDEX -> RIGHT_INDEX
    RIGHT_INDEX -> LEFT_INDEX
    LEFT_THUMB -> RIGHT_THUMB
    RIGHT_THUMB -> LEFT_THUMB
    LEFT_HIP -> RIGHT_HIP
    RIGHT_HIP -> LEFT_HIP
    LEFT_KNEE -> RIGHT_KNEE
    RIGHT_KNEE -> LEFT_KNEE
    LEFT_ANKLE -> RIGHT_ANKLE
    RIGHT_ANKLE -> LEFT_ANKLE
    LEFT_HEEL -> RIGHT_HEEL
    RIGHT_HEEL -> LEFT_HEEL
    LEFT_FOOT_INDEX -> RIGHT_FOOT_INDEX
    RIGHT_FOOT_INDEX -> LEFT_FOOT_INDEX
}
```

`NormalizedPose.kt`:
```kotlin
package com.snowboardpose.shared.pose.normalize

import com.snowboardpose.shared.pose.Joint
import com.snowboardpose.shared.pose.JointType

/**
 * A skeleton frame's joints expressed in body-relative coordinates:
 * origin at the hip center, scaled by torso length (hip-center-to-
 * shoulder-center distance) — translation- and scale-invariant, so the
 * same pose at a different image position or camera distance produces
 * comparable values. [joints] only contains entries that passed the
 * configured visibility threshold; each Joint's visibility is carried
 * through unchanged. [hipCenter]/[shoulderCenter] are exposed in raw
 * (pre-normalization, image-space) coordinates for diagnostics and reuse
 * by later phases (e.g. Phase 6's hip-vertical-displacement feature).
 */
data class NormalizedPose(
    val timestampMillis: Long,
    val hipCenter: Joint,
    val shoulderCenter: Joint,
    val torsoLength: Float,
    val joints: Map<JointType, Joint>,
) {
    fun joint(type: JointType): Joint? = joints[type]
}
```

`PoseNormalizer.kt`:
```kotlin
package com.snowboardpose.shared.pose.normalize

import com.snowboardpose.shared.pose.Joint
import com.snowboardpose.shared.pose.JointType
import com.snowboardpose.shared.pose.PoseProcessingConfig
import com.snowboardpose.shared.pose.SkeletonFrame

/**
 * Converts a raw [SkeletonFrame] into a [NormalizedPose]. Returns `null`
 * — not an exception — when normalization isn't possible for this frame
 * (a required anchor joint is missing/below threshold, or the computed
 * torso is degenerate). This is an expected, frequent runtime outcome
 * during continuous camera tracking (person partially out of frame,
 * turned away, etc.), not a data-corruption error like Phase 3's
 * `PoseSequenceParseException` — so it is modeled as a normal return
 * value, not a thrown exception, avoiding exception-based control flow
 * for something that will happen every few frames in real use.
 */
object PoseNormalizer {

    private const val MIN_TORSO_LENGTH = 1e-4f

    private val requiredJointTypes = setOf(
        JointType.LEFT_HIP, JointType.RIGHT_HIP,
        JointType.LEFT_SHOULDER, JointType.RIGHT_SHOULDER,
    )

    fun normalize(
        frame: SkeletonFrame,
        config: PoseProcessingConfig = PoseProcessingConfig(),
        mirrored: Boolean = false,
    ): NormalizedPose? {
        val visible = frame.joints.filterValues { it.visibility >= config.visibilityThreshold }
        if (!requiredJointTypes.all { it in visible }) return null

        val hipCenter = midpoint(visible.getValue(JointType.LEFT_HIP), visible.getValue(JointType.RIGHT_HIP))
        val shoulderCenter = midpoint(visible.getValue(JointType.LEFT_SHOULDER), visible.getValue(JointType.RIGHT_SHOULDER))
        val torsoLength = distance2D(hipCenter, shoulderCenter)
        if (torsoLength < MIN_TORSO_LENGTH) return null

        val relative = visible.mapValues { (_, joint) ->
            Joint(
                x = (joint.x - hipCenter.x) / torsoLength,
                y = (joint.y - hipCenter.y) / torsoLength,
                z = (joint.z - hipCenter.z) / torsoLength,
                visibility = joint.visibility,
            )
        }

        val finalJoints = if (mirrored) {
            relative.entries.associate { (type, joint) -> type.mirrored() to joint.copy(x = -joint.x) }
        } else {
            relative
        }

        return NormalizedPose(frame.timestampMillis, hipCenter, shoulderCenter, torsoLength, finalJoints)
    }
}
```

Design decisions worth calling out explicitly (resolved during planning,
documented here so a future session doesn't re-litigate them):

- **Visibility thresholding + missing-joint handling**: joints below
  `config.visibilityThreshold`, or absent from the frame entirely, are
  silently excluded from the output map — never crash, never a null
  field (consistent with `SkeletonFrame`'s existing "missing joint =
  absent map entry" convention). If any of the 4 required anchor joints
  is missing or below threshold, `normalize()` returns `null` for the
  whole frame.
- **Degenerate-torso guard**: `torsoLength < MIN_TORSO_LENGTH` (hip and
  shoulder centers coincide — a corrupt/degenerate frame) also returns
  `null` rather than dividing by ~zero. `MIN_TORSO_LENGTH` is a
  placeholder epsilon, not empirically tuned — flagged the same way
  Phase 2 flagged `PoseProcessingConfig`'s defaults.
- **`null`, not an exception, for normalization failure**: see the
  `PoseNormalizer` KDoc above — this is a deliberate, load-bearing
  distinction from Phase 3's parse-exception pattern, not an
  inconsistency.
- **Mirroring order and correctness**: `x` is negated **after**
  translating to hip-relative coordinates (i.e., in the already-centered
  body-relative space), not in raw image space. This is the correct
  choice, not just a convenient one: it mirrors the body through its own
  central vertical axis (the hip center), which is what "undo
  front-camera mirroring" actually needs. Mirroring in raw image space
  (e.g. `1 - x` around the image's own centerline) would instead reflect
  through the photograph's frame center — unrelated to where the subject
  is standing, and wrong for this purpose. Because reflection commutes
  with translation and uniform scaling, negating `x` post-normalization
  is mathematically equivalent to mirroring the raw pose around the hip
  center's own position, which is exactly the desired behavior, and
  simpler to implement. The joint-label relabeling
  (`JointType.mirrored()`) is applied in the same pass and is necessary
  *in addition to* the coordinate negation: if the camera mirrored the
  image, MediaPipe's anatomically-fixed landmark indices get "fooled" —
  it reports the subject's actual right shoulder under the `LEFT_SHOULDER`
  index, at the mirrored `x` position. Negating `x` alone would place it
  correctly but leave it mislabeled; relabeling alone would fix the label
  but leave the position wrong. Both together are required and correctly
  compose in a single pass. `mirrored` is a caller-supplied `Boolean` (not
  derived from `SkeletonFrame`, which per Phase 2 deliberately carries no
  mirroring metadata) — Phase 8/14's camera adapters will be the ones
  passing this flag once they exist.
- **"Coordinate-system normalization" and "coordinate-axis conversion"**:
  per Phase 2's architecture decision (platform axis/orientation handling
  lives in adapters, never in shared code), this phase adds **no**
  platform-specific axis-flip logic. "Coordinate-system normalization" is
  read as *being* the hip-relative/torso-scaled transform itself — not a
  separate step. The master plan's "coordinate-axis conversion" unit-test
  bullet and "Android-style vs iOS-style fixtures normalize identically"
  integration-test bullet are satisfied by proving the normalizer is pure,
  platform-agnostic relative-geometry math with no hidden per-platform
  branching (see Unit/Integration tests below) — not by adding conversion
  code nothing currently needs.

**Files to create:**
```
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/normalize/PoseGeometry.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/normalize/JointMirroring.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/normalize/NormalizedPose.kt
shared/src/commonMain/kotlin/com/snowboardpose/shared/pose/normalize/PoseNormalizer.kt

shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/normalize/PoseGeometryTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/normalize/JointMirroringTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/normalize/PoseNormalizerTest.kt
shared/src/commonTest/kotlin/com/snowboardpose/shared/pose/normalize/PoseNormalizerIntegrationTest.kt
```

**Files to modify:** None required for functionality. `PoseProcessingConfig`
already exposes `visibilityThreshold`; no change needed. (`Plan Files/milestone_roadmap.md`'s
status row will be updated as part of the completion report, per
precedent — not an implementation-time file change.)

**Files to delete:** None.

**Dependencies:** None new. `kotlin.math.sqrt` is stdlib. No okio, no
kotlinx-datetime, no new Gradle module.

**Implementation steps:**
1. Create the `commonMain` package directory `pose/normalize/`.
2. Create `PoseGeometry.kt` (`midpoint`, `distance2D`).
3. Create `JointMirroring.kt` (`JointType.mirrored()`, all 33 cases).
4. Create `NormalizedPose.kt`.
5. Create `PoseNormalizer.kt` (`normalize()`, with the required-joint
   check, degenerate-torso guard, transform, and mirroring).
6. Create the `commonTest` package directory `pose/normalize/`.
7. Write `PoseGeometryTest.kt`.
8. Write `JointMirroringTest.kt`.
9. Write `PoseNormalizerTest.kt`.
10. Write `PoseNormalizerIntegrationTest.kt`, reusing
    `pose.replay.PoseFixtures.idleFrames`/`movementFrames` where a fixture
    is needed, plus new same-position/different-scale/mirrored variants
    built the same way (see Integration tests below for what these need
    to assert).
11. Run `./gradlew :shared:jvmTest`; fix failures.
12. Run `./gradlew :shared:testAndroidHostTest`; fix any Android-target-only
    issues.
13. Run `./gradlew build` to confirm `androidApp` still assembles.
14. Report using the master plan's Section 9 format; stop (do not start
    Phase 5 automatically).

**Unit tests:**
- `PoseGeometryTest`:
  - `midpoint_returnsAverageOfXYZ`
  - `midpoint_visibilityIsMinimumOfInputs`
  - `distance2D_knownPoints_returnsExpectedDistance`
  - `distance2D_ignoresZ` (two joints differing only in `z` → distance 0)
  - `distance2D_samePoint_returnsZero`
- `JointMirroringTest`:
  - `mirrored_pairedJoints_swapLeftAndRight` (parameterized/looped over
    all 16 pairs, or one assertion per pair)
  - `mirrored_nose_mapsToItself`
  - `mirrored_isInvolution` (`joint.mirrored().mirrored() == joint` for
    all 33 entries — cheap way to assert the table has no typos/one-way
    mappings)
- `PoseNormalizerTest`:
  - `normalize_validFrame_hipCenterIsMidpointOfHips`
  - `normalize_validFrame_shoulderCenterIsMidpointOfShoulders`
  - `normalize_validFrame_torsoLengthMatchesDistance2D`
  - `normalize_jointBelowVisibilityThreshold_excludedFromOutput`
  - `normalize_jointAbsentFromFrame_excludedFromOutput`
  - `normalize_missingRequiredHip_returnsNull`
  - `normalize_missingRequiredShoulder_returnsNull`
  - `normalize_degenerateTorso_returnsNull` (hip center == shoulder
    center)
  - `normalize_translatedPose_producesIdenticalOutput` (same pose, all
    x/y shifted by a constant offset → identical `NormalizedPose.joints`,
    proving translation invariance)
  - `normalize_scaledPose_producesIdenticalOutput` (same pose, all
    x/y scaled around a fixed reference point by a constant factor →
    identical `NormalizedPose.joints` within floating-point tolerance,
    proving scale invariance)
  - `normalize_mirrored_negatesXAndSwapsLeftRightLabels`
  - `normalize_mirroredTwice_returnsToOriginal` (mirroring is its own
    inverse)
  - `normalize_nonMirrored_leavesLabelsAndSignUnchanged`
  - `normalize_yAxisFlippedInput_producesConsistentRelativeGeometry` (a
    synthetic pose with `y` values flipped relative to a baseline still
    yields the same `torsoLength` and correctly-signed mirroring —
    proving the normalizer hardcodes no particular axis convention; this
    is the "coordinate-axis conversion" bullet, satisfied as
    convention-agnosticism rather than an actual conversion feature — see
    Technical design)

**Integration tests** (`PoseNormalizerIntegrationTest`, `commonTest`, runs
under both `:shared:jvmTest` and `:shared:testAndroidHostTest`):
- *Raw recorded frames normalize into expected values*:
  `PoseNormalizer.normalize(frame)` on each frame of
  `pose.replay.PoseFixtures.idleFrames` produces a non-null
  `NormalizedPose` with the exact expected `hipCenter`/`torsoLength`
  computed by hand from the fixture's known joint values.
- *Android-style and iOS-style coordinate fixtures normalize identically*:
  two hand-built `SkeletonFrame`s with byte-identical joint values, one
  local variable named `androidStyleFrame` and one `iosStyleFrame` purely
  to document intent (both are actually the same convention — MediaPipe's
  normalized-image-space output is platform-independent by construction;
  see Technical design's "coordinate-system normalization" note) →
  `PoseNormalizer.normalize(...)` produces exactly equal `NormalizedPose`
  values for both, proving no hidden platform branching exists.
- *Partially missing frames remain safe*: a frame built from
  `PoseFixtures.idleFrames.first()` with one non-required joint (e.g.
  `LEFT_ANKLE`) removed still normalizes successfully, simply omitting
  that joint from the output; a frame with a required joint (e.g.
  `LEFT_HIP`) removed returns `null` without throwing.

**System tests:**
- *Diagnostic application displays stable normalized values for a
  stationary person*: satisfied by
  `normalize_translatedPose_producesIdenticalOutput` (unit test above) —
  a "stationary person" whose raw coordinates jitter slightly frame-to-frame
  due to landmark noise is exactly the translation-invariance property
  being proven; there is no on-device diagnostic UI this phase (see Out
  of scope). Documented explicitly in the test's KDoc, same mapping
  technique Phase 3 used for its "test-runner workflow" system-test bullet.
- *Moving closer to the camera does not significantly change normalized
  body proportions*: satisfied by `normalize_scaledPose_producesIdenticalOutput`
  — "moving closer" is modeled as a uniform scale-up of all raw
  coordinates around the hip center, and the test asserts the normalized
  output is unchanged (within floating-point tolerance).

**Performance tests:** None required this phase — `normalize()` is O(joint
count) per frame with only arithmetic, no allocation-heavy work beyond one
output map; not yet run in a hot loop (that starts in Phase 8/9's live
camera pipeline). Becomes meaningful once real per-frame throughput is
measurable, same framing Phases 2/3 used for deferring performance tests.

**Validation commands:**
```
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew build
```

**Risks:**
- The "coordinate-axis conversion" and "Android-style vs iOS-style
  fixtures" master-plan bullets are satisfied by proving
  convention-agnosticism/determinism rather than by building an actual
  axis-conversion feature (see Technical design's justification, grounded
  in Phase 2's explicit architecture decision). If a future phase
  discovers real Android/iOS MediaPipe output actually differs in axis
  convention (not just in which platform produced it), that difference
  must be normalized in the *platform adapter* (Phase 8/9 or 14/15)
  before constructing a `SkeletonFrame`, not retrofitted into
  `PoseNormalizer` — flagging so this isn't mistaken for an oversight
  later.
- `MIN_TORSO_LENGTH = 1e-4f` is an unvalidated placeholder epsilon, not
  empirically tuned against real MediaPipe output noise — same category
  of flagged-placeholder as `PoseProcessingConfig`'s existing defaults.
  May need revisiting once Phase 8/9 produce real jittery hip/shoulder
  data.
- `distance2D` deliberately ignores `z` (see Out of scope) — if a later
  phase starts populating `worldJoints` with real metric depth and wants
  3D-aware torso length, that's a deliberate future change to
  `PoseNormalizer`, not an oversight in this phase.
- Mirroring is unit-tested as a pure geometric/relabeling transform but
  has no real front-camera input to validate against yet (no camera
  integration exists until Milestone 3) — correctness rests on the
  geometric argument documented above, not on empirical validation
  against a real mirrored capture. Flag for re-verification once Phase
  8/9's camera adapters exist and can supply a real mirrored fixture.
- This plan was written one session ahead of implementation (per the
  user's request) without a live back-and-forth review cycle on the
  design — a future implementing session should still treat this as a
  presented-but-unapproved plan (re-confirm current repo state hasn't
  drifted, and get explicit approval) before writing any code, per the
  master plan's standing process.

**Completion criteria:**
- All new files compile for both `android` and `jvm` targets
  (`./gradlew build` succeeds).
- All unit/integration tests pass under both `:shared:jvmTest` and
  `:shared:testAndroidHostTest`.
- Same pose at different image positions produces comparable (identical,
  within float tolerance) normalized output.
- Same pose at different scales produces comparable normalized output.
- Tests cover both mirrored and non-mirrored input.
- `PoseNormalizer.normalize` handles missing/low-visibility joints and
  missing required anchor joints without throwing.
- No platform-specific axis-conversion logic added to `commonMain`
  (verified by inspection) — consistent with Phase 2's architecture
  decision.
- No new dependencies, no changes to Phase 2/3 model types.

---

## Verification (once Phase 4 is implemented)

1. `./gradlew :shared:jvmTest` — all new geometry/mirroring/normalizer/
   integration tests pass.
2. `./gradlew :shared:testAndroidHostTest` — same tests pass under the
   Android target.
3. `./gradlew build` — full project (including `androidApp`) still
   compiles.
4. Report results using the master plan's Section 9 format (created/
   modified files, dependencies added, test results, build results, known
   limitations, recommended next phase — Phase 5: Joint Filtering and
   Temporal Buffer, per Milestone 2).

**This plan is not yet approved.** A future session must present it (as
written here, or revised if repo state has drifted) and get explicit
user approval before implementing, per the master plan's unchanged
process — plans are never auto-implemented across a session boundary.
