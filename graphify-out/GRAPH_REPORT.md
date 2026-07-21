# Graph Report - .  (2026-07-21)

## Corpus Check
- Corpus is ~12,119 words - fits in a single context window. You may not need a graph.

## Summary
- 83 nodes · 100 edges · 15 communities (10 shown, 5 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 4 edges (avg confidence: 0.82)
- Token cost: 100,000 input · 18,174 output

## Community Hubs (Navigation)
- Cross-Platform Module Setup
- Shared Kotlin Greeting + Android Activity
- iOS Deferral and Later Milestones
- Movement Detection State Machines
- Pose Data Model
- Milestone 1 Plan and Report
- CameraX Capture Pipeline
- Gradle Wrapper Script
- Agent Workflow Formats
- Gradle Daemon Heap Fix
- Continuous Verification Rules
- Definition of Done
- Development Principles
- Testing Strategy

## God Nodes (most connected - your core abstractions)
1. `androidApp Module` - 8 edges
2. `Milestone 2 — Shared Movement Detection Core` - 8 edges
3. `Shared Kotlin Multiplatform Module` - 7 edges
4. `Milestone 3 — Android MVP` - 7 edges
5. `MainActivity` - 6 edges
6. `Phase 1: Base Kotlin Multiplatform Project Structure` - 6 edges
7. `Phase 2: Shared Pose Data Model` - 6 edges
8. `Milestone 4 — iOS MVP` - 6 edges
9. `Milestone 1 Completion Report` - 5 edges
10. `iosApp Module` - 5 edges

## Surprising Connections (you probably didn't know these)
- `MainActivity` --implements--> `androidApp Module`  [EXTRACTED]
  androidApp/src/main/kotlin/com/snowboardpose/app/MainActivity.kt → Plan Files/cross_platform_movement_detection_master_plan.md
- `Jetpack Compose (Compose BOM 2026.06.01)` --references--> `MainActivity`  [EXTRACTED]
  Plan Files/milestone_1_completion_report.md → androidApp/src/main/kotlin/com/snowboardpose/app/MainActivity.kt
- `MainActivity` --calls--> `Greeting (shared/src/commonMain)`  [EXTRACTED]
  androidApp/src/main/kotlin/com/snowboardpose/app/MainActivity.kt → Plan Files/milestone_1_completion_report.md
- `kotlin-test 2.4.10` --references--> `GreetingTest`  [EXTRACTED]
  Plan Files/milestone_1_completion_report.md → shared/src/commonTest/kotlin/com/snowboardpose/shared/GreetingTest.kt
- `GreetingTest` --calls--> `Greeting (shared/src/commonMain)`  [EXTRACTED]
  shared/src/commonTest/kotlin/com/snowboardpose/shared/GreetingTest.kt → Plan Files/milestone_1_completion_report.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Planned Cross-Platform Technology Stack** — plan_files_cross_platform_movement_detection_master_plan_mediapipe_pose_landmarker, plan_files_cross_platform_movement_detection_master_plan_kotlin_multiplatform, plan_files_cross_platform_movement_detection_master_plan_camerax, plan_files_cross_platform_movement_detection_master_plan_avfoundation [EXTRACTED 1.00]
- **Shared Pose Data Model Components** — plan_files_cross_platform_movement_detection_master_plan_jointtype, plan_files_cross_platform_movement_detection_master_plan_joint, plan_files_cross_platform_movement_detection_master_plan_skeletonframe, plan_files_cross_platform_movement_detection_master_plan_movementtype, plan_files_cross_platform_movement_detection_master_plan_movementevent [EXTRACTED 1.00]
- **Milestone 1 Plan/Execute/Report Documentation Chain** — plan_files_cross_platform_movement_detection_master_plan, plan_files_milestone_roadmap, plan_files_milestone_1_plan, plan_files_milestone_1_completion_report [EXTRACTED 1.00]

## Communities (15 total, 5 thin omitted)

### Community 0 - "Cross-Platform Module Setup"
Cohesion: 0.20
Nodes (11): androidApp/src/main/AndroidManifest.xml, androidApp Module, iosApp Module, Kotlin Multiplatform, MediaPipe Pose Landmarker, Phase 1: Base Kotlin Multiplatform Project Structure, Phase 15: iOS MediaPipe Pose Integration, Phase 9: Android MediaPipe Pose Integration (+3 more)

### Community 1 - "Shared Kotlin Greeting + Android Activity"
Cohesion: 0.17
Nodes (8): MainActivity, Bundle, ComponentActivity, Jetpack Compose (Compose BOM 2026.06.01), kotlin-test 2.4.10, Greeting (shared/src/commonMain), sharedGreeting(), GreetingTest

### Community 2 - "iOS Deferral and Later Milestones"
Cohesion: 0.17
Nodes (12): AVFoundation, Phase 13: iOS Base Application, Phase 14: iOS Camera Integration, Phase 16: Cross-Platform Consistency, Phase 17: Calibration and Configuration, Phase 18: Performance Optimization, Phase 19: Reliability Testing, Phase 20: Packaging and Release Preparation (+4 more)

### Community 3 - "Movement Detection State Machines"
Cohesion: 0.20
Nodes (11): Hop Detection State Machine (STANDING/RISING/AIRBORNE/LANDING), Phase 0: Repository and Environment Assessment, Phase 3: Recorded Pose Input and Test Harness, Phase 4: Pose Validation and Normalization, Phase 5: Joint Filtering and Temporal Buffer, Phase 6: Hop Detection, Phase 7: Turn-Around Detection, Turn-Around Detection State Sequence (FRONT/SIDE/BACK/SIDE/FRONT) (+3 more)

### Community 4 - "Pose Data Model"
Cohesion: 0.48
Nodes (7): Joint data class, JointType enum, MovementEvent data class, MovementType enum, Phase 2: Shared Pose Data Model, Intended Processing Pipeline, SkeletonFrame data class

### Community 5 - "Milestone 1 Plan and Report"
Cohesion: 0.53
Nodes (6): Cross-Platform Human Movement Detection Master Plan, Milestone 1 Completion Report, Milestone 1 Plan (Phase 0 Report + Phase 1 Plan), Phase 1 Plan — Base KMP Project Structure (Android-only), Toolchain Setup via Android Studio (not bare CLI toolchain), Milestone Roadmap

### Community 6 - "CameraX Capture Pipeline"
Cohesion: 0.33
Nodes (6): CameraX, Phase 10: Android Skeleton Overlay and Diagnostics, Phase 11: Android Movement Integration, Phase 12: Pose Recording and Export, Phase 8: Android Camera Integration, Milestone 3 — Android MVP

### Community 7 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 8 - "Agent Workflow Formats"
Cohesion: 1.00
Nodes (3): Agent Responsibilities (per-phase plan/approve/implement/report loop), Required Phase Completion Report Format (Section 9), Required Phase Plan Format (Section 8)

## Knowledge Gaps
- **21 isolated node(s):** `Kotlin Multiplatform`, `Mandatory Testing Strategy (unit/integration/system/regression/performance)`, `Definition of Done`, `Continuous Verification Rules`, `Phase 3: Recorded Pose Input and Test Harness` (+16 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Milestone 2 — Shared Movement Detection Core` connect `Movement Detection State Machines` to `Pose Data Model`, `CameraX Capture Pipeline`?**
  _High betweenness centrality (0.266) - this node is a cross-community bridge._
- **Why does `Phase 1: Base Kotlin Multiplatform Project Structure` connect `Cross-Platform Module Setup` to `Movement Detection State Machines`, `Milestone 1 Plan and Report`?**
  _High betweenness centrality (0.249) - this node is a cross-community bridge._
- **Why does `androidApp Module` connect `Cross-Platform Module Setup` to `Shared Kotlin Greeting + Android Activity`, `CameraX Capture Pipeline`?**
  _High betweenness centrality (0.210) - this node is a cross-community bridge._
- **What connects `Kotlin Multiplatform`, `Mandatory Testing Strategy (unit/integration/system/regression/performance)`, `Definition of Done` to the rest of the system?**
  _21 weakly-connected nodes found - possible documentation gaps or missing edges._