# Milestone Roadmap
## Derived from cross_platform_movement_detection_master_plan.md

This groups the master plan's 21 phases (Phase 0–20) into 6 milestones so a
new session can pick up a single milestone and have everything needed to plan
from, without re-reading the full master plan first. Full phase detail
(objective, expected work, tests, completion criteria) is reproduced below
per phase — this file is self-contained.

The master plan's process still applies to every phase, unchanged:

1. Inspect current repo state.
2. Present a full phase plan (objective, scope, files, design, tests, risks,
   completion criteria) using the format in Section 8 of the master plan.
3. **Stop and wait for explicit approval before touching any files.**
4. Implement only the approved phase.
5. Run all applicable tests/builds, report results using the format in
   Section 9 of the master plan.
6. Stop. Do not start the next phase automatically.

A milestone is just a grouping for tracking/demo purposes — it does not
bundle phases into one approval. Each phase inside it is still planned,
approved, and reported on individually.

**Every plan produced for this project — milestone overviews and individual
phase plans alike — must be written into this `Plan Files/` directory**
(e.g. `milestone_2_plan.md`), not only to the coding agent's own private
plan-mode scratch file. This keeps the plan record part of the repo itself,
consistent with `milestone_1_plan.md`/`milestone_1_completion_report.md`,
so a future session can reconstruct full planning history without relying
on any single agent session's local state.

---

## Milestone 1 — Foundation & Environment
**Phases:** 0, 1
**Goal:** A working, empty-but-real cross-platform skeleton: repo assessed,
toolchain confirmed, minimal Android app and minimal iOS app both build and
both call into a shared Kotlin Multiplatform module.
**Exit demo:** Android app launches showing a value from `shared`; iOS app
launches showing the same value from `shared`.

### Phase 0: Repository and Environment Assessment
**Objective:** Inspect the repository and development environment without
modifying application code.
**Expected work:** Identify repository structure, Gradle/Kotlin/Java
versions, Android SDK configuration, existing Android modules, existing KMP
configuration, existing iOS project files, Xcode requirements, test
frameworks, CI configuration, code style/lint tools; inspect version-control
state; identify missing prerequisites.
**Unit tests:** None required — no application logic changes.
**Integration tests:** None required.
**System tests:** Run existing build/test commands if available, to
establish a baseline.
**Completion criteria:** Repository assessment completed; existing
tests/builds executed where possible; baseline failures documented;
recommended phase order produced; detailed Phase 1 plan produced; no
application source files modified.

### Phase 1: Base Kotlin Multiplatform Project Structure
**Objective:** Create or validate the cross-platform project structure.
**Expected result:** Shared KMP module; Android application module; iOS
application target; minimal Android app launches; minimal iOS app builds;
shared code callable from both platforms; test source sets configured.
**Unit tests:** Shared common test runs successfully; simple shared function
returns the same value on supported targets; platform abstractions compile.
**Integration tests:** Android app calls shared Kotlin code; iOS app calls
shared Kotlin code; shared module framework generation succeeds for iOS.
**System tests:** Android app installs and launches; iOS app launches in
simulator or on device; both apps display a value produced by the shared
module.
**Completion criteria:** Android build passes; iOS build passes; shared
tests pass; shared code proven callable from Android and iOS.

---

## Milestone 2 — Shared Movement Detection Core
**Phases:** 2, 3, 4, 5, 6, 7
**Goal:** All movement-recognition logic (pose model, recorded-fixture test
harness, normalization, filtering, hop detector, turn detector) built and
fully unit/integration tested in the shared module — with zero camera or
platform dependency. Fully testable on a desktop/CI machine before any
mobile work begins.
**Exit demo:** Feeding recorded fixtures through the shared pipeline
correctly emits HOP / TURN_AROUND / NONE events, with passing unit +
integration test suites.

### Phase 2: Shared Pose Data Model
**Objective:** Create shared models for joints, skeleton frames, movement
events, and processing configuration.
**Expected components:** Joint type enum; joint coordinate model; skeleton
frame model; movement type enum; movement event model; configuration
models; validation utilities; serialization format where required.
**Unit tests:** Joint values stored correctly; missing joints handled;
invalid visibility values rejected/normalized; movement confidence
boundaries enforced; equality/copy behavior correct; serialization
round-trip preserves data; unknown/missing serialized fields handled
safely.
**Integration tests:** Android adapter constructs a shared skeleton model
from a fixture; iOS adapter contract constructs the same logical skeleton;
recorded fixture loads into shared models.
**System tests:** Not required beyond confirming both apps still build with
the shared model.
**Completion criteria:** Models compile on all targets; unit tests pass;
serialization stable and documented; no platform-specific types leak into
shared code.

### Phase 3: Recorded Pose Input and Test Harness
**Objective:** Build a deterministic movement-processing test harness
independent of live camera input.
**Expected capabilities:** Load recorded pose landmark sequences; replay
frames in timestamp order; feed frames into the processing pipeline;
inspect intermediate values; record detector output; support deterministic
tests.
**Unit tests:** JSON/CSV parser handles valid input; parser rejects
malformed input; frames ordered correctly; duplicate timestamps handled;
missing timestamps rejected/reconstructed per design; replay timing logic
deterministic.
**Integration tests:** Recorded file loads into `SkeletonFrame` objects;
loaded sequence passes through a placeholder pipeline; expected frame count
reaches the detector; output events captured correctly.
**System tests:** CLI or test-runner workflow processes a complete fixture;
a known fixture produces a known output summary.
**Completion criteria:** At least one idle fixture and one synthetic
movement fixture usable; harness runs in CI/normal test command; no camera
device required for movement-logic testing.

### Phase 4: Pose Validation and Normalization
**Objective:** Prepare raw skeleton data for reliable movement recognition.
**Expected processing:** Visibility thresholding; missing-joint handling;
hip/shoulder center calculation; torso length calculation; body-relative
normalization; coordinate-system normalization; mirroring normalization
where needed.
**Unit tests:** Midpoint/distance/torso-length calculations; visibility
rejection; missing required joint behavior; translation invariance; scale
invariance; mirroring transformation; coordinate-axis conversion.
**Integration tests:** Raw recorded frames normalize into expected values;
Android-style and iOS-style coordinate fixtures normalize identically;
partially missing frames remain safe.
**System tests:** Diagnostic app displays stable normalized values for a
stationary person; moving closer to the camera doesn't significantly
change normalized body proportions.
**Completion criteria:** Same pose at different image positions/scales
produces comparable normalized output; tests cover mirrored and
non-mirrored input.

### Phase 5: Joint Filtering and Temporal Buffer
**Objective:** Reduce landmark jitter and store recent frame history.
**Expected components:** Exponential moving average filter; configurable
smoothing factor; per-joint filter state; missing-frame tolerance; temporal
frame buffer; velocity calculation; optional acceleration calculation;
angle normalization utilities.
**Unit tests:** Filter output for fixed sequences; alpha boundary behavior;
reset behavior; missing-frame behavior; buffer capacity/eviction behavior;
velocity from irregular timestamps; zero-time-difference protection; angle
wraparound.
**Integration tests:** Recorded noisy sequence becomes smoother; filtered
output feeds feature extraction; temporary missing landmarks don't corrupt
future values; buffer preserves chronological order.
**System tests:** Stationary person produces low measured velocity; slow
movement remains visible after filtering; fast movement not excessively
delayed.
**Performance tests:** Filter processing cost per frame; memory usage
remains bounded by buffer size.
**Completion criteria:** Jitter measurably reduced; buffer never grows
without limit; irregular frame intervals handled correctly.

### Phase 6: Hop Detection
**Objective:** Recognize a basic hop using temporal pose information.
**Suggested state machine:** STANDING → RISING → AIRBORNE → LANDING →
STANDING.
**Expected features:** Hip vertical displacement/velocity; left/right
ankle displacement; both-feet movement; rising/airborne/landing phases;
detection cooldown; rejection of walking and crouching.
**Unit tests:** Valid/invalid state transitions; timeout behavior; cooldown
behavior; minimum displacement/velocity thresholds; missing ankle/hip
handling; reset after pose loss; single event emitted per hop.
**Integration tests (recorded sequences):** Valid hop; small bounce; crouch
without jumping; one-leg lift; walking; noisy hop; hop with brief landmark
loss; slow hop; fast hop.
**System tests:** Ten real hops produce expected detections; ten crouches
produce no hop detections; ten steps produce no hop detections; no
duplicate events; detection works at multiple distances.
**Performance tests:** Detector execution time per frame; no allocations
that grow with runtime.
**Completion criteria:** Defined minimum true-positive rate and maximum
false-positive rate in controlled tests; one event per valid hop; test
fixtures stored for regression testing.

### Phase 7: Turn-Around Detection
**Objective:** Recognize a complete turn around.
**Suggested state sequence:** FRONT → SIDE → BACK → SIDE → FRONT.
**Expected features:** Shoulder/hip orientation; shoulder-width changes;
left-right joint ordering; world-coordinate depth values when available;
face-landmark visibility; temporary landmark-loss tolerance; accumulated
body rotation; clockwise/counterclockwise support; detection cooldown.
**Unit tests:** Orientation calculation; angle accumulation/wraparound;
front/side/back classification; clockwise/counterclockwise sequence;
half-turn rejection; side-and-return rejection; missing-landmark
tolerance; cooldown behavior; single event per full turn.
**Integration tests (recorded sequences):** Full clockwise turn; full
counterclockwise turn; half turn; sideways turn and return; walking across
frame; arms crossing body; back-facing pause; temporary pose loss; camera
shake; turn at different speeds.
**System tests:** Ten clockwise turns; ten counterclockwise turns; ten half
turns; ten side-and-return movements; portrait and landscape orientation;
front and rear camera; multiple distances.
**Completion criteria:** Full turns detected in both directions; half turns
rejected; temporary landmark loss doesn't always reset detection; false
positives within defined limits.

---

## Milestone 3 — Android MVP
**Phases:** 8, 9, 10, 11, 12
**Goal:** Real camera → real MediaPipe → real shared detector → real UI,
running end-to-end on an Android device, including skeleton overlay and
recording/export of sequences for regression fixtures.
**Exit demo:** A person performs a hop and a turn in front of an Android
phone; the app detects and displays both, without duplicate events.

### Phase 8: Android Camera Integration
**Objective:** Capture camera frames on Android and display a live preview.
**Expected technology:** Kotlin, CameraX, Preview use case, ImageAnalysis
use case, runtime permissions, latest-frame backpressure strategy.
**Unit tests:** Camera configuration mapping; rotation conversion;
lens-facing configuration; permission-state mapping; analyzer lifecycle
state logic.
**Integration tests:** CameraX preview/analyzer bind to lifecycle; frame
analyzer receives frames; frame timestamps increase; old frames dropped
when processing slower; front-camera mirroring metadata correct.
**System tests:** Permission granted/denied/permanently-denied flows; front
camera starts; rear camera starts; rotation change; background/foreground
recovery; camera unavailable error.
**Performance tests:** Preview/analyzer frame rate; bounded frame backlog;
stable memory during extended preview.
**Completion criteria:** Stable live preview; analyzer receives frames; UI
remains responsive; no unbounded frame queue.

### Phase 9: Android MediaPipe Pose Integration
**Objective:** Run MediaPipe Pose Landmarker on Android frames.
**Expected behavior:** Model loads; live-stream mode configured; frames
convert correctly; rotation/mirroring handled; inference asynchronous;
results convert into shared `SkeletonFrame`; errors logged without
crashing.
**Unit tests:** Landmark index mapping; MediaPipe result conversion;
visibility/timestamp/rotation metadata conversion; no-person result
handling; partial-pose result handling.
**Integration tests:** Camera frame enters MediaPipe; result enters shared
pipeline; fixture result produces expected shared skeleton; model
init/shutdown work repeatedly; lifecycle restart doesn't duplicate
analyzers.
**System tests:** Person visible produces skeleton; no person produces no
skeleton; partial body doesn't crash; rotation works; front-camera
mirroring works; app recovers after backgrounding.
**Performance tests:** Inference latency; effective pose frame rate;
CPU/GPU utilization; memory stability; thermal behavior during extended
use.
**Completion criteria:** Real-time pose data available; shared frames have
correct coordinates; no crashes during normal lifecycle transitions.

### Phase 10: Android Skeleton Overlay and Diagnostics
**Objective:** Display landmarks, skeleton connections, detector state, and
performance information.
**Unit tests:** Coordinate-to-screen transformation; mirroring/rotation
transformation; connection rendering data; diagnostic value formatting.
**Integration tests:** Shared skeleton renders over preview; overlay aligns
with body; orientation changes update overlay; missing joints skipped
safely.
**System tests:** Overlay alignment in portrait/landscape; front-camera
mirror alignment; rear-camera alignment; diagnostic overlay can be
enabled/disabled.
**Performance tests:** Overlay rendering time; UI frame rate with overlay
enabled; memory allocations during rendering.
**Completion criteria:** Skeleton visually aligns with person; diagnostics
don't block inference; production mode can disable debug information.

### Phase 11: Android Movement Integration
**Objective:** Connect Android pose output to the shared movement pipeline.
**Unit tests:** (mostly covered by shared detector tests) UI event mapping;
event deduplication; detector reset on session restart; configuration
update behavior.
**Integration tests:** Camera → MediaPipe → shared pipeline → UI; hop
fixture through Android adapter; turn fixture through Android adapter;
temporary pose loss and recovery; lifecycle restart resets/restores state
per design.
**System tests:** Complete real hop detection; complete real turn
detection; no repeated event spam; clear feedback in UI; session reset
works.
**Completion criteria:** End-to-end Android prototype works; both target
movements recognized; detection events visible; known limitations
documented.

### Phase 12: Pose Recording and Export
**Objective:** Record skeleton sequences for testing and detector
improvement.
**Expected capabilities:** Start/stop recording; record skeleton data;
label movement type; export recordings; import recordings into test
harness; avoid raw video by default.
**Unit tests:** Recording state transitions; frame serialization; file
naming; metadata validation; empty recording handling; interrupted
recording recovery.
**Integration tests:** Live skeleton frames recorded; exported file loads
in the shared test harness; replayed output matches original frame
count/timestamps; labels preserved.
**System tests:** User records a hop sequence; user exports recording; test
harness replays recording; detector produces expected event.
**Completion criteria:** Recordings reproducible; recording format
documented; recorded files can become regression fixtures.

---

## Milestone 4 — iOS MVP
**Phases:** 13, 14, 15
**Goal:** The same shared detector proven out on iOS: base app wired to
`shared`, AVFoundation camera capture, MediaPipe pose inference on-device.
**Exit demo:** Real-time pose skeleton data flowing on an iPhone/simulator
into the shared Kotlin detector.
**Environment risk:** This milestone requires a macOS machine with Xcode.
If development is happening on Linux, this must be resolved (separate Mac,
cloud Mac, or CI runner) before Milestone 4 starts — flag this explicitly
during Phase 0.

### Phase 13: iOS Base Application
**Objective:** Create the iOS application and connect it to shared Kotlin
code.
**Unit tests:** Swift wrapper conversions; shared data access; nullability
behavior; enum mapping.
**Integration tests:** Swift calls shared Kotlin functions; shared movement
detector can be instantiated; shared test fixture can be processed from
iOS code.
**System tests:** iOS app launches; shared value appears in UI; app
survives background/foreground transitions.
**Completion criteria:** Shared module works from Swift; iOS build
reproducible; interoperability limitations documented.

### Phase 14: iOS Camera Integration
**Objective:** Capture camera frames on iPhone.
**Expected technology:** AVFoundation, `AVCaptureSession`,
`AVCaptureVideoDataOutput`, camera permissions, SwiftUI/UIKit preview
integration.
**Unit tests:** Permission-state mapping; orientation conversion; camera
selection logic; mirroring metadata; session-state logic.
**Integration tests:** Capture session starts/stops; preview and frame
output work together; frames reach the analyzer queue; old frames
discarded when required.
**System tests:** Permission granted/denied; front/rear camera; rotation;
background/foreground recovery; camera unavailable condition.
**Performance tests:** Preview/analyzer frame rate; memory stability; frame
backlog behavior.
**Completion criteria:** Stable preview; frames delivered for analysis; no
unbounded queue; correct orientation metadata.

### Phase 15: iOS MediaPipe Pose Integration
**Objective:** Run MediaPipe Pose Landmarker on iPhone frames.
**Unit tests:** Landmark mapping; visibility/timestamp/orientation
conversion; no-person handling; partial-pose handling.
**Integration tests:** Camera frame enters MediaPipe; pose result converts
to shared skeleton; shared detector receives iOS frames; model
init/shutdown work repeatedly.
**System tests:** Skeleton detected for visible person; no-person case;
partial-body case; front-camera mirroring; rotation; background/foreground
recovery.
**Performance tests:** Inference latency; effective pose frame rate;
CPU/GPU use; memory stability; thermal behavior.
**Completion criteria:** Real-time pose data available on iOS; shared
detector receives correct coordinates; platform differences documented.

---

## Milestone 5 — Cross-Platform Convergence & Calibration
**Phases:** 16, 17
**Goal:** Android and iOS proven to produce compatible pose data and
detection results within defined tolerances; optional per-user calibration
flow added to improve reliability without becoming a hard requirement.
**Exit demo:** Same person performs the same movement on both platforms;
detection type, timing, and confidence are compared and within tolerance.
**Prerequisite:** Milestones 3 and 4 both complete (needs both platforms
working).

### Phase 16: Cross-Platform Consistency
**Objective:** Ensure Android and iOS produce compatible pose data and
movement results.
**Unit tests:** Shared coordinate conversion fixtures; shared mirroring
fixtures; shared timestamp normalization; shared landmark identifier
mapping.
**Integration tests:** Same logical fixture through Android adapter and iOS
adapter; normalized output compared within tolerance; detector events
compared within timing tolerance.
**System tests:** Same user performs same movement on Android and iPhone;
compare detection event type, timing, confidence, false-positive behavior.
**Completion criteria:** Platform differences remain within defined
tolerances; shared detector doesn't require duplicated platform-specific
logic; any required platform configuration documented.

### Phase 17: Calibration and Configuration
**Objective:** Improve reliability across different users and camera
setups.
**Possible calibration flow:** User stands still facing camera → system
measures torso length → records baseline hip/ankle positions → checks
full-body visibility → stores session thresholds → optional example hop →
optional example turn.
**Unit tests:** Calibration state transitions; baseline calculation;
threshold derivation; invalid calibration rejection; calibration timeout;
reset behavior; configuration persistence where applicable.
**Integration tests:** Calibration output changes detector configuration;
calibrated detector processes recorded fixtures; calibration data survives
expected lifecycle transitions.
**System tests:** Multiple users calibrate successfully; calibration fails
clearly when body not visible; detection improves or remains stable after
calibration; reset to defaults works.
**Completion criteria:** Calibration optional unless proven necessary;
calibration doesn't make detection less reliable; configuration values
documented.

---

## Milestone 6 — Performance, Reliability & Release
**Phases:** 18, 19, 20
**Goal:** Performance-tuned, tested under adverse real-world conditions
(lighting, distance, occlusion, orientation), and packaged as a signed,
installable release build on both platforms.
**Exit demo:** Signed Android and iOS release builds pass the full system
test suite under documented conditions; reliability report published.

### Phase 18: Performance Optimization
**Objective:** Improve real-time performance without changing detector
behavior.
**Investigation areas:** Camera resolution; inference frame rate; frame
skipping; GPU/CPU delegate; model complexity; memory allocation; object
reuse; logging overhead; overlay cost; shared-module conversion overhead.
**Unit tests:** Optimized code preserves numerical outputs; configuration
validation; frame-skipping logic; object-pool reset behavior where used.
**Integration tests:** Detector outputs before/after optimization match
within tolerance; pipeline remains stable under extended input; lifecycle
handling remains correct.
**System tests:** Extended Android run; extended iOS run; repeated
background/foreground transitions; continuous movement detection; thermal
throttling observation.
**Performance tests:** Baseline vs. optimized latency; effective frame
rate; CPU/GPU use; peak memory; memory growth over time; battery
consumption; device temperature.
**Completion criteria:** Performance improvements measured; detection
behavior remains equivalent; no new instability introduced.

### Phase 19: Reliability Testing
**Objective:** Test under realistic and adverse conditions.
**Test conditions:** Bright/dark room; backlighting; cluttered/plain
background; loose clothing; partial occlusion; near/far distance;
portrait/landscape; front/rear camera; different body sizes; fast/slow
movement; camera vibration; temporary pose loss.
**Unit tests:** Regression tests added for issues found during reliability
testing.
**Integration tests:** Replay all captured failure cases through the shared
pipeline.
**System tests:** For each supported movement/condition, record attempts,
true positives, false positives, false negatives, detection delay,
pose-loss rate, inference frame rate.
**Completion criteria:** Reliability report created; known failure
conditions documented; critical failures have regression tests; supported
operating conditions defined.

### Phase 20: Packaging and Release Preparation
**Objective:** Prepare the application for internal testing or public
distribution.
**Expected work:** Production build configuration; application
identifiers; app name/icons; permission descriptions; privacy
documentation; error reporting policy; release signing; Android App
Bundle; iOS archive; versioning; basic user instructions.
**Unit tests:** Version parsing; configuration selection; privacy-setting
defaults; release-only feature flags.
**Integration tests:** Release build includes correct model/assets;
debug-only diagnostics excluded/disabled; shared module version matches
application version; permission descriptions present.
**System tests:** Install clean release build; upgrade from previous
internal version; first-launch permissions; camera startup; hop detection;
turn detection; background/foreground behavior; uninstall/reinstall
behavior.
**Completion criteria:** Signed release artifacts build successfully;
release system tests pass; privacy behavior documented; known limitations
included in release notes.

---

## Sequencing notes

- Milestones 1 and 2 have no camera/mobile dependency and should be
  completed first — they de-risk the actual movement-detection logic before
  any platform integration work starts.
- Milestone 3 (Android) is sequenced before Milestone 4 (iOS) because the
  current dev environment is Linux, where Android tooling works natively but
  iOS builds are impossible without a separate macOS machine. This should be
  confirmed/resolved before Milestone 4 starts.
- Milestones 5 and 6 require both platforms to exist and should not start
  until Milestones 3 and 4 are both complete.

## Status

| Milestone | Phases | Status |
|---|---|---|
| 1 — Foundation & Environment | 0–1 | Complete — see `milestone_1_plan.md` and `milestone_1_completion_report.md` |
| 2 — Shared Movement Detection Core | 2–7 | Phases 2–3 complete (see `milestone_2_plan.md`/`milestone_2_phase_2_completion_report.md` and `milestone_2_phase_3_plan.md`/`milestone_2_phase_3_completion_report.md`); Phases 4–7 not started |
| 3 — Android MVP | 8–12 | Not started |
| 4 — iOS MVP | 13–15 | Not started |
| 5 — Cross-Platform Convergence & Calibration | 16–17 | Not started |
| 6 — Performance, Reliability & Release | 18–20 | Not started |

## How to start a new session on Milestone 1

Open a new session in this repo and point it at this file plus the master
plan. The first instruction to give it should be exactly what the master
plan's Section 14 specifies: begin with Phase 0 only, inspect the repo and
dev environment, do not modify application code, and produce the Phase 0
report + a detailed, approval-pending Phase 1 plan.
