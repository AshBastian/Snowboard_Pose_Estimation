# Cross-Platform Human Movement Detection  
## Master Plan for an AI Coding Agent

## 1. Project Goal

Build a cross-platform mobile application that uses the camera of an Android or iPhone device to detect a human body pose in real time and recognize basic movements from body-joint data.

Initial target movements:

- Hop
- Full turn around
- Idle or no recognized movement

The application should process camera data locally on the device where practical.

The planned technology stack is:

- MediaPipe Pose Landmarker for skeleton extraction
- Kotlin Multiplatform for shared movement-processing logic
- Kotlin and CameraX for Android
- Swift, SwiftUI, and AVFoundation for iOS
- Platform-specific MediaPipe integration on Android and iOS

The AI coding agent must not immediately implement the complete system. It must divide the work into phases and create a detailed implementation plan before every phase.

---

# 2. Agent Responsibilities

For every phase, the coding agent must:

1. Inspect the current repository state.
2. Identify existing files, modules, dependencies, and architecture.
3. Define the exact objective of the phase.
4. Define what is in scope and out of scope.
5. List every file that will be created, modified, or deleted.
6. Describe the intended technical implementation.
7. Identify technical risks and unresolved decisions.
8. Define unit, integration, and system tests.
9. Define measurable completion criteria.
10. Present the phase plan before changing files.
11. Wait for explicit approval before implementation.
12. Implement only the approved phase.
13. Run all applicable tests and build checks.
14. Report test results and remaining limitations.
15. Stop before starting the next phase.

The agent must not:

- Make unrelated architectural changes
- Silently replace frameworks or dependencies
- Change files not listed in the approved plan, except unavoidable generated or lock files
- Skip tests because the feature appears simple
- Continue to the next phase automatically
- Mark a phase complete when tests are missing or failing without documenting the reason

---

# 3. Development Principles

The agent must follow these principles:

- Prefer small, verifiable changes.
- Keep platform-specific code outside the shared module.
- Keep movement detection deterministic where possible.
- Use timestamps instead of assuming a fixed frame rate.
- Treat missing and low-confidence landmarks explicitly.
- Avoid global mutable state.
- Keep camera, pose extraction, preprocessing, detection, and UI separated.
- Document coordinate systems and units.
- Avoid unnecessary dependencies.
- Add regression tests for every fixed defect where practical.
- Preserve reproducibility for movement-sequence tests.
- Avoid storing raw camera images unless explicitly enabled.
- Keep all processing local unless a later approved phase adds networking.

---

# 4. Required System Architecture

```text
shared/
    pose data models
    coordinate normalization
    landmark validation
    joint filtering
    temporal pose history
    feature extraction
    movement detection
    movement state machines
    shared test fixtures
    shared unit tests
    shared integration tests

androidApp/
    CameraX integration
    Android permissions
    Android lifecycle handling
    MediaPipe Android integration
    frame conversion
    skeleton overlay
    Android system tests

iosApp/
    AVFoundation integration
    iOS permissions
    iOS lifecycle handling
    MediaPipe iOS integration
    frame conversion
    skeleton overlay
    iOS system tests
```

The shared module must not directly depend on:

- Android `Context`
- Android `Bitmap`
- CameraX types
- UIKit types
- AVFoundation types
- Platform-specific MediaPipe result types

Platform-specific MediaPipe results must be converted into shared data objects before movement processing.

---

# 5. Shared Data Model

The shared module should define platform-neutral pose data.

Example conceptual model:

```kotlin
enum class JointType {
    NOSE,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX
}

data class Joint(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

data class SkeletonFrame(
    val timestampMillis: Long,
    val joints: Map<JointType, Joint>
)

enum class MovementType {
    NONE,
    HOP,
    TURN_AROUND
}

data class MovementEvent(
    val type: MovementType,
    val timestampMillis: Long,
    val confidence: Float
)
```

The final model must support:

- Normalized image coordinates
- Optional world coordinates
- Visibility or confidence values
- Timestamps
- Missing landmarks
- Camera orientation metadata when needed
- Front-camera mirroring information when needed
- Stable serialization for recorded test sequences

---

# 6. Intended Processing Pipeline

```text
Camera frame
    ↓
Platform camera API
    ↓
Platform-specific frame conversion
    ↓
MediaPipe Pose Landmarker
    ↓
Platform-specific pose result
    ↓
Conversion into shared SkeletonFrame
    ↓
Landmark validation
    ↓
Coordinate normalization
    ↓
Joint smoothing
    ↓
Temporal frame buffer
    ↓
Feature extraction
    ↓
Movement state machines
    ↓
Movement event
    ↓
User interface, logging, and optional recording
```

Initial limitations:

- One person only
- No cloud processing
- No multi-camera support
- No machine-learning movement classifier
- No background camera processing

---

# 7. Mandatory Testing Strategy

Every phase must contain appropriate tests from the following levels.

## 7.1 Unit Tests

Unit tests verify isolated functions or classes.

Examples:

- Distance calculations
- Midpoint calculations
- Angle normalization
- Coordinate transformations
- Visibility filtering
- Joint smoothing
- Velocity calculation
- State-machine transitions
- Cooldown handling
- Serialization and deserialization
- Missing-joint behavior

Unit tests must:

- Be deterministic
- Avoid camera hardware
- Avoid network access
- Use fixed timestamps and fixed inputs
- Include normal cases, edge cases, and invalid input
- Run automatically in the normal build pipeline

## 7.2 Integration Tests

Integration tests verify interactions between multiple modules or components.

Examples:

- MediaPipe result conversion into `SkeletonFrame`
- Recorded sequence loading into the movement pipeline
- Normalization followed by filtering and feature extraction
- Hop detector using real recorded skeleton sequences
- Turn detector using real recorded skeleton sequences
- Android camera analyzer passing frames into the MediaPipe adapter
- iOS frame pipeline passing results into shared Kotlin code

Integration tests must:

- Verify data contracts between components
- Verify timestamp continuity
- Verify coordinate orientation and mirroring
- Verify errors propagate without crashes
- Use recorded or synthetic fixtures where hardware is unnecessary
- Use device or simulator tests when native APIs are required

## 7.3 System Tests

System tests verify the complete working application from camera input to user-visible result.

Examples:

- Launch application
- Grant camera permission
- Display camera preview
- Detect one person
- Draw skeleton overlay
- Recognize one hop
- Recognize one full turn
- Avoid duplicate movement events
- Recover after pose loss
- Resume correctly after application backgrounding

System tests must define:

- Device or simulator
- Operating-system version
- Camera used
- Lighting conditions
- Person distance
- Expected output
- Allowed timing tolerance
- Pass and fail criteria

Automated system tests should be used where practical. Physical movement tests may remain manual, but they must be documented and repeatable.

## 7.4 Regression Tests

Every bug fix should include a test that fails before the fix and passes after the fix whenever practical.

## 7.5 Performance Tests

Performance tests must measure:

- Pose-inference latency
- Processing frame rate
- Dropped frame behavior
- Memory growth
- CPU usage
- GPU usage when applicable
- Thermal throttling during extended use
- Battery impact during longer tests

## 7.6 Test Reporting

After every implemented phase, report:

```text
Unit tests:
Integration tests:
System tests:
Performance tests:
Tests passed:
Tests failed:
Tests skipped:
Reason for skipped tests:
Build status:
Known untested areas:
```

A phase must not be marked complete when required tests fail.

---

# 8. Required Phase Plan Format

Before implementation, the coding agent must return:

```text
Phase:
Objective:
Current state:
Scope:
Out of scope:
Technical design:
Files to create:
Files to modify:
Files to delete:
Dependencies:
Implementation steps:
Unit tests:
Integration tests:
System tests:
Performance tests:
Validation commands:
Risks:
Completion criteria:
```

The agent must then stop and wait for approval.

---

# 9. Required Phase Completion Report

After implementation, the coding agent must return:

```text
Completed work:
Created files:
Modified files:
Deleted files:
Dependencies added:
Unit tests executed:
Integration tests executed:
System tests executed:
Performance tests executed:
Build commands:
Build results:
Test results:
Manual validation:
Known limitations:
Problems encountered:
Regression tests added:
Recommended next phase:
```

The agent must not start the next phase automatically.

---

# 10. Project Phases

## Phase 0: Repository and Environment Assessment

### Objective

Inspect the repository and development environment without modifying application code.

### Expected Work

- Identify repository structure
- Identify Gradle version
- Identify Kotlin version
- Identify Java version
- Identify Android SDK configuration
- Identify existing Android modules
- Identify Kotlin Multiplatform configuration
- Identify existing iOS project files
- Identify Xcode requirements
- Identify test frameworks
- Identify CI configuration
- Identify code style and lint tools
- Inspect version-control state
- Identify missing prerequisites

### Unit Tests

None required because no application logic is changed.

### Integration Tests

None required.

### System Tests

Run existing build and test commands if available to establish a baseline.

### Completion Criteria

- Repository assessment completed
- Existing tests and builds executed where possible
- Baseline failures documented
- Recommended phase order produced
- Detailed Phase 1 plan produced
- No application source files modified

---

## Phase 1: Base Kotlin Multiplatform Project Structure

### Objective

Create or validate the cross-platform project structure.

### Expected Result

- Shared Kotlin Multiplatform module
- Android application module
- iOS application target
- Minimal Android application launches
- Minimal iOS application builds
- Shared code callable from both platforms
- Test source sets configured

### Unit Tests

- Shared common test runs successfully
- Simple shared function returns the same value on supported targets
- Platform abstractions compile correctly

### Integration Tests

- Android application calls shared Kotlin code
- iOS application calls shared Kotlin code
- Shared module framework generation succeeds for iOS

### System Tests

- Android app installs and launches
- iOS app launches in simulator or on device
- Both applications display a value produced by the shared module

### Completion Criteria

- Android build passes
- iOS build passes
- Shared tests pass
- Shared code is proven callable from Android and iOS

---

## Phase 2: Shared Pose Data Model

### Objective

Create shared models for joints, skeleton frames, movement events, and processing configuration.

### Expected Components

- Joint type enumeration
- Joint coordinate model
- Skeleton frame model
- Movement type enumeration
- Movement event model
- Configuration models
- Validation utilities
- Serialization format where required

### Unit Tests

- Joint values are stored correctly
- Missing joints are handled
- Invalid visibility values are rejected or normalized
- Movement confidence boundaries are enforced
- Equality and copy behavior work as intended
- Serialization round-trip preserves data
- Unknown or missing serialized fields are handled safely

### Integration Tests

- Android adapter can construct a shared skeleton model from a fixture
- iOS adapter contract can construct the same logical skeleton
- Recorded fixture can be loaded into shared models

### System Tests

Not required beyond confirming both applications still build with the shared model.

### Completion Criteria

- Models compile on all targets
- Unit tests pass
- Serialization is stable and documented
- No platform-specific types leak into shared code

---

## Phase 3: Recorded Pose Input and Test Harness

### Objective

Build a deterministic movement-processing test harness independent of live camera input.

### Expected Capabilities

- Load recorded pose landmark sequences
- Replay frames in timestamp order
- Feed frames into the processing pipeline
- Inspect intermediate values
- Record detector output
- Support deterministic tests

### Unit Tests

- JSON or CSV parser handles valid input
- Parser rejects malformed input
- Frames are ordered correctly
- Duplicate timestamps are handled
- Missing timestamps are rejected or reconstructed according to the design
- Replay timing logic is deterministic

### Integration Tests

- Recorded file loads into `SkeletonFrame` objects
- Loaded sequence passes through a placeholder pipeline
- Expected number of frames reaches the detector
- Output events are captured correctly

### System Tests

- Command-line or test-runner workflow processes a complete fixture
- A known fixture produces a known output summary

### Completion Criteria

- At least one idle fixture and one synthetic movement fixture are usable
- Test harness runs in CI or the normal test command
- No camera device is required for movement-logic testing

---

## Phase 4: Pose Validation and Normalization

### Objective

Prepare raw skeleton data for reliable movement recognition.

### Expected Processing

- Visibility thresholding
- Missing-joint handling
- Hip center calculation
- Shoulder center calculation
- Torso length calculation
- Body-relative normalization
- Coordinate-system normalization
- Mirroring normalization where needed

### Unit Tests

- Midpoint calculations
- Distance calculations
- Torso-length calculation
- Visibility rejection
- Missing required joint behavior
- Translation invariance
- Scale invariance
- Mirroring transformation
- Coordinate-axis conversion

### Integration Tests

- Raw recorded frames normalize into expected values
- Android-style and iOS-style coordinate fixtures normalize identically
- Partially missing frames remain safe

### System Tests

- Diagnostic application displays stable normalized values for a stationary person
- Moving closer to the camera does not significantly change normalized body proportions

### Completion Criteria

- Same pose at different image positions produces comparable normalized output
- Same pose at different scales produces comparable normalized output
- Tests cover mirrored and non-mirrored input

---

## Phase 5: Joint Filtering and Temporal Buffer

### Objective

Reduce landmark jitter and store recent frame history.

### Expected Components

- Exponential moving average filter
- Configurable smoothing factor
- Per-joint filter state
- Missing-frame tolerance
- Temporal frame buffer
- Velocity calculation
- Optional acceleration calculation
- Angle normalization utilities

### Unit Tests

- Filter output for fixed sequences
- Alpha boundary behavior
- Reset behavior
- Missing-frame behavior
- Buffer capacity behavior
- Old-frame eviction
- Velocity from irregular timestamps
- Zero-time-difference protection
- Angle wraparound

### Integration Tests

- Recorded noisy sequence becomes smoother
- Filtered output feeds feature extraction
- Temporary missing landmarks do not corrupt future values
- Buffer preserves chronological order

### System Tests

- Stationary person produces low measured velocity
- Slow movement remains visible after filtering
- Fast movement is not excessively delayed

### Performance Tests

- Filter processing cost per frame
- Memory usage remains bounded by buffer size

### Completion Criteria

- Jitter is measurably reduced
- Buffer never grows without limit
- Irregular frame intervals are handled correctly

---

## Phase 6: Hop Detection

### Objective

Recognize a basic hop using temporal pose information.

### Suggested State Machine

```text
STANDING
    ↓
RISING
    ↓
AIRBORNE
    ↓
LANDING
    ↓
STANDING
```

### Expected Features

- Hip vertical displacement
- Hip vertical velocity
- Left and right ankle displacement
- Both-feet movement
- Rising phase
- Airborne phase
- Landing phase
- Detection cooldown
- Rejection of walking and crouching

### Unit Tests

- Valid state transitions
- Invalid transition rejection
- Timeout behavior
- Cooldown behavior
- Minimum displacement threshold
- Minimum velocity threshold
- Missing ankle handling
- Missing hip handling
- Reset after pose loss
- Single event emitted per hop

### Integration Tests

Recorded sequences:

- Valid hop
- Small bounce
- Crouch without jumping
- One-leg lift
- Walking
- Noisy hop
- Hop with brief landmark loss
- Slow hop
- Fast hop

### System Tests

- Ten real hops produce expected detections
- Ten crouches produce no hop detections
- Ten steps produce no hop detections
- Duplicate events are not emitted
- Detection works at multiple distances

### Performance Tests

- Detector execution time per frame
- No allocations that grow with runtime

### Completion Criteria

- Defined minimum true-positive rate in controlled tests
- Defined maximum false-positive rate in controlled tests
- One event emitted per valid hop
- Test fixtures stored for regression testing

---

## Phase 7: Turn-Around Detection

### Objective

Recognize a complete turn around.

### Suggested State Sequence

```text
FRONT
    ↓
SIDE
    ↓
BACK
    ↓
SIDE
    ↓
FRONT
```

### Expected Features

- Shoulder orientation
- Hip orientation
- Shoulder-width changes
- Left-right joint ordering
- World-coordinate depth values when available
- Face-landmark visibility
- Temporary landmark-loss tolerance
- Accumulated body rotation
- Clockwise and counterclockwise support
- Detection cooldown

### Unit Tests

- Orientation calculation
- Angle accumulation
- Angle wraparound
- Front, side, and back classification
- Clockwise sequence
- Counterclockwise sequence
- Half-turn rejection
- Side-and-return rejection
- Missing-landmark tolerance
- Cooldown behavior
- Single event per full turn

### Integration Tests

Recorded sequences:

- Full clockwise turn
- Full counterclockwise turn
- Half turn
- Sideways turn and return
- Walking across frame
- Arms crossing body
- Back-facing pause
- Temporary pose loss
- Camera shake
- Turn at different speeds

### System Tests

- Ten clockwise turns
- Ten counterclockwise turns
- Ten half turns
- Ten side-and-return movements
- Tests in portrait and landscape orientation
- Tests with front and rear camera
- Tests at multiple distances

### Completion Criteria

- Full turns detected in both directions
- Half turns rejected
- Temporary landmark loss does not always reset detection
- False positives remain within defined limits

---

## Phase 8: Android Camera Integration

### Objective

Capture camera frames on Android and display a live preview.

### Expected Technology

- Kotlin
- CameraX
- Preview use case
- ImageAnalysis use case
- Runtime permissions
- Latest-frame backpressure strategy

### Unit Tests

- Camera configuration mapping
- Rotation conversion
- Lens-facing configuration
- Permission-state mapping
- Analyzer lifecycle state logic

### Integration Tests

- CameraX preview and analyzer bind to lifecycle
- Frame analyzer receives frames
- Frame timestamps increase
- Old frames are dropped when processing is slower
- Front-camera mirroring metadata is correct

### System Tests

- Permission granted flow
- Permission denied flow
- Permission permanently denied flow
- Front camera starts
- Rear camera starts
- Rotation change
- Background and foreground recovery
- Camera unavailable error

### Performance Tests

- Preview frame rate
- Analyzer frame rate
- Frame backlog remains bounded
- Memory remains stable during extended preview

### Completion Criteria

- Stable live preview
- Analyzer receives frames
- UI remains responsive
- No unbounded frame queue

---

## Phase 9: Android MediaPipe Pose Integration

### Objective

Run MediaPipe Pose Landmarker on Android frames.

### Expected Behavior

- Model loads
- Live-stream mode is configured
- Frames convert correctly
- Rotation and mirroring are handled
- Inference is asynchronous
- Results convert into shared `SkeletonFrame`
- Errors are logged without crashing

### Unit Tests

- Landmark index mapping
- MediaPipe result conversion
- Visibility conversion
- Timestamp conversion
- Rotation metadata conversion
- No-person result handling
- Partial-pose result handling

### Integration Tests

- Camera frame enters MediaPipe
- MediaPipe result enters shared pipeline
- Fixture result produces expected shared skeleton
- Model initialization and shutdown work repeatedly
- Lifecycle restart does not duplicate analyzers

### System Tests

- Person visible produces skeleton
- No person produces no skeleton
- Partial body does not crash
- Rotation works
- Front-camera mirroring works
- App recovers after backgrounding

### Performance Tests

- Inference latency
- Effective pose frame rate
- CPU and GPU utilization
- Memory stability
- Thermal behavior during extended use

### Completion Criteria

- Real-time pose data available
- Shared frames have correct coordinates
- No crashes during normal lifecycle transitions

---

## Phase 10: Android Skeleton Overlay and Diagnostics

### Objective

Display landmarks, skeleton connections, detector state, and performance information.

### Unit Tests

- Coordinate-to-screen transformation
- Mirroring transformation
- Rotation transformation
- Connection rendering data
- Diagnostic value formatting

### Integration Tests

- Shared skeleton renders over preview
- Overlay aligns with body
- Orientation changes update overlay
- Missing joints are skipped safely

### System Tests

- Overlay alignment in portrait
- Overlay alignment in landscape
- Front-camera mirror alignment
- Rear-camera alignment
- Diagnostic overlay can be enabled and disabled

### Performance Tests

- Overlay rendering time
- UI frame rate with overlay enabled
- Memory allocations during rendering

### Completion Criteria

- Skeleton visually aligns with person
- Diagnostics do not block inference
- Production mode can disable debug information

---

## Phase 11: Android Movement Integration

### Objective

Connect Android pose output to the shared movement pipeline.

### Unit Tests

Covered mainly in shared detector tests.

Additional tests:

- UI event mapping
- Event deduplication
- Detector reset on session restart
- Configuration update behavior

### Integration Tests

- Camera to MediaPipe to shared pipeline to UI
- Hop fixture through Android adapter
- Turn fixture through Android adapter
- Temporary pose loss and recovery
- Lifecycle restart resets or restores state according to design

### System Tests

- Complete real hop detection
- Complete real turn detection
- No repeated event spam
- Clear feedback in UI
- Session reset works

### Completion Criteria

- End-to-end Android prototype works
- Both target movements are recognized
- Detection events are visible
- Known limitations are documented

---

## Phase 12: Pose Recording and Export

### Objective

Record skeleton sequences for testing and detector improvement.

### Expected Capabilities

- Start and stop recording
- Record skeleton data
- Label movement type
- Export recordings
- Import recordings into test harness
- Avoid raw video by default

### Unit Tests

- Recording state transitions
- Frame serialization
- File naming
- Metadata validation
- Empty recording handling
- Interrupted recording recovery

### Integration Tests

- Live skeleton frames are recorded
- Exported file loads in the shared test harness
- Replayed output matches original frame count and timestamps
- Labels are preserved

### System Tests

- User records a hop sequence
- User exports recording
- Test harness replays recording
- Detector produces expected event

### Completion Criteria

- Recordings are reproducible
- Recording format is documented
- Recorded files can become regression fixtures

---

## Phase 13: iOS Base Application

### Objective

Create the iOS application and connect it to shared Kotlin code.

### Unit Tests

- Swift wrapper conversions
- Shared data access
- Nullability behavior
- Enum mapping

### Integration Tests

- Swift calls shared Kotlin functions
- Shared movement detector can be instantiated
- Shared test fixture can be processed from iOS code

### System Tests

- iOS application launches
- Shared value appears in UI
- Application survives background and foreground transitions

### Completion Criteria

- Shared module works from Swift
- iOS build is reproducible
- Interoperability limitations are documented

---

## Phase 14: iOS Camera Integration

### Objective

Capture camera frames on iPhone.

### Expected Technology

- AVFoundation
- `AVCaptureSession`
- `AVCaptureVideoDataOutput`
- Camera permissions
- SwiftUI or UIKit preview integration

### Unit Tests

- Permission-state mapping
- Orientation conversion
- Camera selection logic
- Mirroring metadata
- Session-state logic

### Integration Tests

- Capture session starts and stops
- Preview and frame output work together
- Frames reach the analyzer queue
- Old frames are discarded when required

### System Tests

- Permission granted
- Permission denied
- Front camera
- Rear camera
- Rotation
- Background and foreground recovery
- Camera unavailable condition

### Performance Tests

- Preview frame rate
- Analyzer frame rate
- Memory stability
- Frame backlog behavior

### Completion Criteria

- Stable preview
- Frames delivered for analysis
- No unbounded queue
- Correct orientation metadata

---

## Phase 15: iOS MediaPipe Pose Integration

### Objective

Run MediaPipe Pose Landmarker on iPhone frames.

### Unit Tests

- Landmark mapping
- Visibility conversion
- Timestamp conversion
- Orientation conversion
- No-person handling
- Partial-pose handling

### Integration Tests

- Camera frame enters MediaPipe
- Pose result converts to shared skeleton
- Shared detector receives iOS frames
- Model initialization and shutdown work repeatedly

### System Tests

- Skeleton detected for visible person
- No-person case
- Partial-body case
- Front-camera mirroring
- Rotation
- Background and foreground recovery

### Performance Tests

- Inference latency
- Effective pose frame rate
- CPU/GPU use
- Memory stability
- Thermal behavior

### Completion Criteria

- Real-time pose data available on iOS
- Shared detector receives correct coordinates
- Platform differences documented

---

## Phase 16: Cross-Platform Consistency

### Objective

Ensure Android and iOS produce compatible pose data and movement results.

### Unit Tests

- Shared coordinate conversion fixtures
- Shared mirroring fixtures
- Shared timestamp normalization
- Shared landmark identifier mapping

### Integration Tests

- Same logical fixture through Android adapter and iOS adapter
- Normalized output compared within tolerance
- Detector events compared within timing tolerance

### System Tests

- Same user performs same movement on Android and iPhone
- Compare detection event type
- Compare event timing
- Compare confidence values
- Compare false-positive behavior

### Completion Criteria

- Platform differences remain within defined tolerances
- Shared detector does not require duplicated platform-specific logic
- Any required platform configuration is documented

---

## Phase 17: Calibration and Configuration

### Objective

Improve reliability across different users and camera setups.

### Possible Calibration Flow

1. User stands still facing the camera.
2. System measures torso length.
3. System records baseline hip and ankle positions.
4. System checks full-body visibility.
5. System stores session thresholds.
6. Optional example hop.
7. Optional example turn.

### Unit Tests

- Calibration state transitions
- Baseline calculation
- Threshold derivation
- Invalid calibration rejection
- Calibration timeout
- Reset behavior
- Configuration persistence where applicable

### Integration Tests

- Calibration output changes detector configuration
- Calibrated detector processes recorded fixtures
- Calibration data survives expected lifecycle transitions

### System Tests

- Multiple users calibrate successfully
- Calibration fails clearly when body is not visible
- Detection improves or remains stable after calibration
- Reset to defaults works

### Completion Criteria

- Calibration is optional unless proven necessary
- Calibration does not make detection less reliable
- Configuration values are documented

---

## Phase 18: Performance Optimization

### Objective

Improve real-time performance without changing detector behavior.

### Investigation Areas

- Camera resolution
- Inference frame rate
- Frame skipping
- GPU delegate
- CPU delegate
- Model complexity
- Memory allocation
- Object reuse
- Logging overhead
- Overlay cost
- Shared-module conversion overhead

### Unit Tests

- Optimized code preserves numerical outputs
- Configuration validation
- Frame-skipping logic
- Object-pool reset behavior where used

### Integration Tests

- Detector outputs before and after optimization match within tolerance
- Pipeline remains stable under extended input
- Lifecycle handling remains correct

### System Tests

- Extended Android run
- Extended iOS run
- Repeated background and foreground transitions
- Continuous movement detection
- Thermal throttling observation

### Performance Tests

- Baseline and optimized latency
- Effective frame rate
- CPU use
- GPU use
- Peak memory
- Memory growth over time
- Battery consumption
- Device temperature

### Completion Criteria

- Performance improvements are measured
- Detection behavior remains equivalent
- No new instability introduced

---

## Phase 19: Reliability Testing

### Objective

Test under realistic and adverse conditions.

### Test Conditions

- Bright room
- Dark room
- Backlighting
- Cluttered background
- Plain background
- Loose clothing
- Partial body occlusion
- Person near camera
- Person far from camera
- Portrait orientation
- Landscape orientation
- Front camera
- Rear camera
- Different body sizes
- Fast movement
- Slow movement
- Camera vibration
- Temporary pose loss

### Unit Tests

Add regression tests for issues found during reliability testing.

### Integration Tests

Replay all captured failure cases through the shared pipeline.

### System Tests

For each supported movement and condition, record:

- Attempts
- True positives
- False positives
- False negatives
- Detection delay
- Pose-loss rate
- Inference frame rate

### Completion Criteria

- Reliability report created
- Known failure conditions documented
- Critical failures have regression tests
- Supported operating conditions are defined

---

## Phase 20: Packaging and Release Preparation

### Objective

Prepare the application for internal testing or public distribution.

### Expected Work

- Production build configuration
- Application identifiers
- App name and icons
- Permission descriptions
- Privacy documentation
- Error reporting policy
- Release signing
- Android App Bundle
- iOS archive
- Versioning
- Basic user instructions

### Unit Tests

- Version parsing
- Configuration selection
- Privacy-setting defaults
- Release-only feature flags

### Integration Tests

- Release build includes correct model and assets
- Debug-only diagnostics are excluded or disabled
- Shared module version matches application version
- Permission descriptions are present

### System Tests

- Install clean release build
- Upgrade from previous internal version
- First-launch permissions
- Camera startup
- Hop detection
- Turn detection
- Background and foreground behavior
- Uninstall and reinstall behavior

### Completion Criteria

- Signed release artifacts build successfully
- Release system tests pass
- Privacy behavior is documented
- Known limitations are included in release notes

---

# 11. Initial Functional Requirements

The first usable version must:

- Run on Android
- Show a live camera preview
- Detect one person
- Extract pose landmarks
- Draw the skeleton
- Recognize a hop
- Recognize a full turn around
- Avoid repeated movement events
- Display the recognized movement
- Work without an external server
- Use shared movement-recognition logic suitable for iOS reuse
- Include automated unit tests
- Include recorded-sequence integration tests
- Include documented end-to-end system tests

The first usable version does not need:

- User accounts
- Cloud services
- Video uploads
- Multi-person detection
- Machine-learning movement classification
- Exercise history
- Analytics
- Social features
- Background camera processing
- Production UI design

---

# 12. Definition of Done

A feature is complete only when:

- The approved implementation exists.
- The project builds.
- Relevant unit tests pass.
- Relevant integration tests pass.
- Relevant system tests pass or documented manual system tests have been executed.
- Required performance checks have been executed.
- No known crash occurs in the intended use case.
- Error states are handled.
- The implementation is documented.
- Platform-specific behavior is documented.
- Validation commands are recorded.
- Remaining limitations are listed.
- Regression tests exist for corrected defects where practical.
- The agent provides a completion report.
- No required test is silently skipped.

---

# 13. Continuous Verification Rules

The coding agent must preserve a continuously working project.

After every meaningful implementation step:

1. Run the smallest relevant unit-test set.
2. Run the relevant integration tests.
3. Build the affected target.
4. Run lint or static analysis if configured.
5. Do not continue while newly introduced failures remain unexplained.
6. Add a regression test before or together with a bug fix where practical.
7. Run the complete shared test suite before finishing a shared-code phase.
8. Run the complete Android test suite before finishing an Android phase.
9. Run the complete iOS test suite before finishing an iOS phase.
10. Run end-to-end system tests before declaring a user-visible feature complete.

Recommended verification order:

```text
Unit tests
    ↓
Integration tests
    ↓
Platform build
    ↓
Static analysis
    ↓
System tests
    ↓
Performance checks
```

---

# 14. First Instruction to the Coding Agent

Begin with Phase 0 only.

Inspect the repository and development environment.

Do not create or modify application code.

Produce:

1. Repository structure summary
2. Current build-system summary
3. Existing Android configuration
4. Existing Kotlin Multiplatform configuration
5. Existing iOS configuration
6. Existing unit-test infrastructure
7. Existing integration-test infrastructure
8. Existing system-test infrastructure
9. Existing CI configuration
10. Missing prerequisites
11. Baseline build and test results
12. Technical risks
13. Recommended final phase order
14. Detailed Phase 1 plan
15. Proposed unit, integration, and system tests for Phase 1

Do not implement Phase 1 until the Phase 1 plan has been explicitly approved.
