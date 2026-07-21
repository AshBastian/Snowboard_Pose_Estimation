# Milestone 1 — Phase 1 Completion Report

Format per `cross_platform_movement_detection_master_plan.md` Section 9.

**Completed work:** Stood up a real, buildable Kotlin Multiplatform repo
skeleton: a `shared` module (Android + JVM targets, no iOS — deferred to
Milestone 4 per prior decision) and an `androidApp` module whose UI
displays a value produced by shared code. Verified the full path — shared
code compiles, its tests pass, and the Android app calls it and renders
the result — on a real, physically connected Android device.

**Created files:**
- `settings.gradle.kts`, `build.gradle.kts` (root), `gradle.properties`,
  `.gitignore`, `local.properties`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`,
  `gradlew`, `gradlew.bat`
- `shared/build.gradle.kts`
- `shared/src/commonMain/kotlin/com/snowboardpose/shared/Greeting.kt`
- `shared/src/commonTest/kotlin/com/snowboardpose/shared/GreetingTest.kt`
- `androidApp/build.gradle.kts`
- `androidApp/src/main/AndroidManifest.xml`
- `androidApp/src/main/kotlin/com/snowboardpose/app/MainActivity.kt`

**Modified files:** None (nothing existed before this phase; the plan's
"Files to modify" list was empty and stayed empty — the entries above
represent normal in-progress edits while iterating on a first-time setup,
not modifications to pre-existing files).

**Deleted files:** A stray `java_pid16216.hprof` heap-dump file, produced
accidentally when the default Gradle daemon heap was too small for a lint
task (see Problems encountered) — deleted as build byproduct, not source.

**Dependencies added:**
- Kotlin Multiplatform Gradle plugin 2.4.10
- Android Gradle Plugin 9.3.0 (`com.android.application`,
  `com.android.kotlin.multiplatform.library`)
- Kotlin Compose compiler plugin 2.4.10 (`org.jetbrains.kotlin.plugin.compose`)
- Compose BOM 2026.06.01 (`androidx.compose.ui:ui`,
  `androidx.compose.material3:material3`, `ui-tooling-preview`)
- `androidx.activity:activity-compose` 1.10.1
- `androidx.appcompat:appcompat` 1.7.1
- `kotlin-test` 2.4.10 (shared `commonTest`)
- Gradle wrapper 9.6.1

**Unit tests executed:**
- `:shared:jvmTest` → `GreetingTest.sharedGreeting_returnsExpectedFixedValue` — passed
- `:shared:testAndroidHostTest` → same test, Android-target host-side run — passed

**Integration tests executed:** `androidApp` compiles against `:shared` as
a project dependency and calls `sharedGreeting()` — verified via
`assembleDebug`/`assembleRelease` succeeding and via the system test below
(the call only renders correctly if the dependency wiring is correct end
to end).

**System tests executed (manual):** Built and installed the debug APK via
`./gradlew :androidApp:installDebug` onto a real, physically connected
Android device (model NX809J, Android 16) authorized over USB debugging.
Launched via `adb shell am start`; a screenshot confirms the on-screen
text reads "Hello from shared Kotlin Multiplatform code" — the exact
string returned by the shared module's placeholder function.

**Performance tests executed:** None — not required for this phase
(placeholder code only, per plan).

**Build commands:**
- `./gradlew build`
- `./gradlew :shared:jvmTest`
- `./gradlew :shared:testAndroidHostTest`
- `./gradlew :androidApp:installDebug`

**Build results:** `BUILD SUCCESSFUL` for all of the above (debug and
release variants both assemble).

**Test results:** All executed unit tests passed; no failures.

**Manual validation:** App installed and launched on a physical device;
screenshot evidence collected confirming the shared-module value renders
correctly on screen.

**Known limitations:**
- iOS target intentionally not created — deferred to Milestone 4 per the
  prior user decision (no macOS/Xcode machine available yet).
- No on-device instrumented test exists yet beyond the manual launch;
  `androidHostTest` runs the Android-target unit test on the JVM, not on
  the device itself. Acceptable for this throwaway placeholder phase —
  real instrumented tests should appear once real logic exists (Phase 2+).
- The KMP-on-Android tooling used here (`com.android.kotlin.multiplatform.library`,
  AGP 9's built-in Kotlin support) is very new (introduced January 2026);
  some official documentation examples were already stale for the exact
  AGP/Studio version installed (see Problems encountered).

**Problems encountered (and how they were resolved):**
1. The master plan's original Phase 1 design (`androidTarget()` inside the
   KMP block, combined with the classic `com.android.library` plugin) is
   incompatible with AGP 9.0+, which is what this machine's Android Studio
   installation (Quail 2, stable as of 2026-07-14) bundles. Resolved by
   using the new, AGP9-mandatory `com.android.kotlin.multiplatform.library`
   plugin and its `kotlin { android { ... } }` DSL instead.
2. That plugin's own DSL was renamed between AGP releases — the documented
   `androidLibrary { ... }` block is already deprecated in AGP 9.3.0 in
   favor of `android { ... }`; updated accordingly.
3. `org.jetbrains.kotlin.android` is no longer needed (and is rejected) in
   `androidApp/build.gradle.kts` under AGP 9's built-in Kotlin support;
   removed it. The Kotlin compiler's `jvmTarget` is now derived from
   `compileOptions` rather than a separate `kotlinOptions` block.
4. The default Gradle daemon heap (512MB/384MB metaspace) was too small
   for `lintVitalAnalyzeRelease`'s UAST analysis and crashed with an
   `OutOfMemoryError`. Fixed by adding `gradle.properties` with
   `org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=1024m`.
5. No emulator or physical device was available at the start of this
   phase. Resolved by connecting the user's physical Android device over
   USB and completing the adb authorization (RSA key approval) flow.

**Regression tests added:** None — no defect in existing behavior was
fixed; this phase only created new scaffolding.

**Recommended next phase:** Phase 2 — Shared Pose Data Model (start of
Milestone 2), per `milestone_roadmap.md`. Per the master plan's process,
implementation does not begin automatically — Phase 2 needs its own
presented plan and approval first.
