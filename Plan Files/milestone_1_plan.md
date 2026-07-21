# Milestone 1 — Foundation & Environment (Phase 0 report + Phase 1 plan)

## Context

`milestone_roadmap.md` groups the 21-phase master plan
(`cross_platform_movement_detection_master_plan.md`) into 6 milestones.
Milestone 1 = Phase 0 + Phase 1, and the roadmap's own "how to start a new
session" instructions say to begin with Phase 0 (repo/environment
inspection, no code changes) and end with a detailed, approval-pending
Phase 1 plan — exactly what this document is.

This repo is currently empty (no commits, no app code) — Phase 0 below is
the actual inspection result, not a hypothetical. Two scope decisions were
confirmed with the user before finalizing Phase 1:
- **Toolchain setup:** install Android Studio (bundles JDK + Android SDK +
  emulator manager), rather than a bare command-line toolchain.
- **iOS scope:** deferred entirely out of Phase 1. No macOS/Xcode machine
  exists yet, and the roadmap already flags this as a hard blocker for
  Milestone 4. Phase 1 here only stands up `shared` (Android/JVM targets)
  + `androidApp`, so nothing gets built "blind" and unverified. The iOS
  target gets added in Milestone 4 once a Mac is available.

---

## Phase 0 report — Repository and Environment Assessment

1. **Repository structure:** Git repo initialized on branch `main`, **zero
   commits**. Only content: `Plan Files/` with the planning docs. No
   source code, no build files, no `.gitignore`.
2. **Build system:** None present — no `settings.gradle(.kts)`, no
   `build.gradle(.kts)`, no Gradle wrapper.
3. **Android configuration:** None present.
4. **Kotlin Multiplatform configuration:** None present.
5. **iOS configuration:** None present.
6. **Unit/integration/system test infrastructure:** None present.
7. **CI configuration:** None present (no remote configured either).
8. **Toolchain probe results (this machine, Ubuntu 24.04.4 LTS, x86_64,
   kernel 6.17):**
   - `java`/`javac`: not found; no `/usr/lib/jvm`
   - `gradle`: not found (no global install; irrelevant once wrapper exists)
   - `kotlin` CLI: not found
   - `adb`/`sdkmanager`: not found; no `ANDROID_HOME`/`ANDROID_SDK_ROOT`;
     no `~/Android/Sdk`
   - Android Studio: not installed (checked common paths, `snap list`);
     available to install via `snap install android-studio --classic`
     (confirmed present in the snap store: version 2026.1.2.10)
   - `xcodebuild`: not found (expected — Linux host, no Xcode possible)
   - `curl`/`wget`: available, so downloads for installation are possible
   - `snap`: available; installing packages requires an interactive sudo
     password (not available non-interactively from an agent session)
   - Disk space: 131G available — ample for Android Studio + SDK + emulator
9. **Missing prerequisites:** Everything needed to build a KMP/Android
   project — JDK, Android SDK, Gradle — is missing. Nothing is on this
   machine yet. Android Studio installation resolves JDK + SDK + Gradle
   support together (user-confirmed approach). Installing it requires the
   user to run the `snap install` command themselves (sudo password) and
   complete the first-run setup wizard (SDK license acceptance, component
   downloads) interactively, since these steps need either a password
   prompt or GUI interaction the agent cannot perform.
10. **Baseline build/test results:** N/A — no build files exist to run yet.
11. **Technical risks:**
    - First-time Android Studio setup (SDK component downloads, license
      acceptance) will take real time/disk/network on first run.
    - No macOS machine — iOS is out of scope until Milestone 4; this is
      already flagged in the roadmap and confirmed with the user.
    - Kotlin/AGP/Compose/Gradle versions must be pinned to a known-mutually
      -compatible set to avoid sync failures on a brand-new install.
12. **Recommended phase order:** Unchanged from the roadmap — Milestone 1 →
    2 (shared logic, no device needed) → 3 (Android MVP) → 4 (iOS MVP, once
    a Mac exists) → 5 → 6.

---

## Phase 1 plan — Base Kotlin Multiplatform Project Structure (Android-only)

**Phase:** 1 (Milestone 1, second half)

**Objective:** Stand up a minimal, real, buildable KMP repo skeleton: a
`shared` module (Android + JVM/common source sets — no iOS target yet) and
an `androidApp` module whose UI displays a value produced by shared code.
Prove the shared-code-callable-from-Android path end to end.

**Current state:** Empty repo, no toolchain installed (see Phase 0 above).

**Scope:**
- Install Android Studio (`snap install android-studio --classic`, run by
  the user interactively); let it provision a JDK, Android SDK (platform,
  platform-tools, build-tools), and an emulator image (or use a connected
  physical device if the user prefers).
- Create root Gradle project: `settings.gradle.kts`, root
  `build.gradle.kts`, a version catalog (`gradle/libs.versions.toml`), and
  the Gradle wrapper.
- Create `shared` KMP module: `commonMain`, `androidMain`, `commonTest`
  source sets. Android target + JVM target only (`androidTarget()` +
  `jvm()` in the KMP block) — no `iosTarget`/`iosArm64` etc. yet.
- Create `androidApp` module: single-Activity Jetpack Compose app that
  calls into `shared` and renders the returned value.
- `.gitignore` for Gradle/Android/IDE build artifacts.

**Out of scope:** iOS app/target (Milestone 4), MediaPipe/camera/pose code
(Milestone 2+), CI, app icons/branding, release signing.

**Technical design:**
- `shared` exposes one trivial placeholder, e.g. `fun sharedGreeting():
  String`, proving the module compiles for Android and is callable from
  Kotlin — this is throwaway scaffolding, replaced by real pose models in
  Phase 2 (already scoped in Milestone 2 of the roadmap).
- `androidApp` is a single Activity using Compose (`setContent { Text(...)
  }`) calling `sharedGreeting()` — simplest way to prove the wiring
  end-to-end without building real UI yet.
- Suggested applicationId/package: `com.snowboardpose.app` (root package
  `com.snowboardpose`) — trivial to rename later if the user wants
  something different.
- Version catalog pins one mutually-compatible, current-stable set:
  Kotlin (multiplatform plugin), Android Gradle Plugin, Compose BOM,
  Gradle wrapper version — exact pinned numbers chosen at implementation
  time to the latest stable releases that are known compatible with each
  other.

**Files to create:**
- `settings.gradle.kts`, `build.gradle.kts` (root), `gradle/libs.versions.toml`,
  `gradle/wrapper/gradle-wrapper.properties` (+ wrapper jar/script),
  `gradlew`, `gradlew.bat`
- `shared/build.gradle.kts`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/Greeting.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/GreetingTest.kt`
- `androidApp/build.gradle.kts`
- `androidApp/src/main/AndroidManifest.xml`
- `androidApp/src/main/kotlin/com/snowboardpose/app/MainActivity.kt`
- `.gitignore`

**Files to modify:** None (nothing exists yet).
**Files to delete:** None.

**Dependencies:** Kotlin Multiplatform Gradle plugin, Android Gradle
Plugin, Jetpack Compose (androidApp only), `kotlin-test` (commonTest).

**Implementation steps:**
1. User installs Android Studio and completes the first-run setup wizard
   (SDK license acceptance, component downloads); confirm `adb`, an SDK
   platform, and build-tools end up present; confirm a device or emulator
   is reachable.
2. Scaffold the Gradle multi-module structure and wrapper.
3. Add `shared` module with `commonMain`/`androidMain`/`commonTest`
   source sets and the placeholder function.
4. Add `androidApp` module wired to `shared` as a dependency, with a
   minimal Compose Activity.
5. Sync and build via Gradle; fix any version-compatibility errors.
6. Run shared unit tests; run/install the Android app.

**Unit tests:**
- `shared` commonTest: placeholder function returns the expected fixed
  value; runs under `commonTest` (verifies the shared test source set
  itself is wired correctly).

**Integration tests:**
- `androidApp` (or an `androidUnitTest`/instrumented test) calls
  `sharedGreeting()` from Android context and gets the same value shared
  tests expect — proves the Android→shared dependency wiring.

**System tests (manual):**
- Install and launch `androidApp` on emulator or physical device; confirm
  the shared-produced value is visible on screen.

**Performance tests:** None required — placeholder code only.

**Validation commands:**
- `./gradlew build`
- `./gradlew :shared:testDebugUnitTest` (or equivalent shared test task)
- `./gradlew :androidApp:installDebug` (or run from Android Studio)

**Risks:**
- First-run SDK/component downloads may be slow or hit license-acceptance
  prompts that need interactive handling.
- Version-catalog choices may need adjustment if a pinned combination
  turns out incompatible — will be resolved during implementation, not
  before.

**Completion criteria (Android-only version of the roadmap's Phase 1
criteria):**
- Android build passes via Gradle.
- Shared module's tests pass.
- Shared code is proven callable from `androidApp`.
- iOS target explicitly **not** created — logged as deferred to Milestone 4,
  not as an oversight.

---

## Verification (end-to-end, once Phase 1 is implemented)

1. `./gradlew build` from repo root — full project compiles.
2. `./gradlew :shared:test` (or the generated equivalent task name) —
   shared unit test(s) pass.
3. Launch `androidApp` on an emulator or connected device (via Android
   Studio "Run" or `./gradlew :androidApp:installDebug` + manual launch)
   and confirm the shared-module value renders on screen.
4. Report results using the master plan's Section 9 completion-report
   format (created/modified files, dependencies added, test results,
   build results, known limitations, recommended next phase — which will
   be Phase 2: Shared Pose Data Model, per Milestone 2).

No implementation happens until this Phase 1 plan is explicitly approved,
per the master plan's process (Section 2, item 11).
