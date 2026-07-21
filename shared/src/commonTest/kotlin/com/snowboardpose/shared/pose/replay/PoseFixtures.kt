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

    private fun frameAt(index: Int, hipY: Float, ankleY: Float = 0.90f): SkeletonFrame =
        SkeletonFrame(
            timestampMillis = index * 33L,
            joints = mapOf(
                JointType.LEFT_SHOULDER to Joint(0.45f, 0.20f, 0f, 0.95f),
                JointType.RIGHT_SHOULDER to Joint(0.55f, 0.20f, 0f, 0.95f),
                JointType.LEFT_HIP to Joint(0.45f, hipY, 0f, 0.95f),
                JointType.RIGHT_HIP to Joint(0.55f, hipY, 0f, 0.95f),
                JointType.LEFT_ANKLE to Joint(0.45f, ankleY, 0f, 0.9f),
                JointType.RIGHT_ANKLE to Joint(0.55f, ankleY, 0f, 0.9f),
            ),
        )

    // 9 frames, 33ms apart (~30fps), hip Y essentially static with tiny
    // jitter (0.50 +/- 0.003) — a standing-still person.
    val idleFrames: List<SkeletonFrame> = listOf(
        frameAt(0, 0.500f),
        frameAt(1, 0.503f),
        frameAt(2, 0.498f),
        frameAt(3, 0.501f),
        frameAt(4, 0.497f),
        frameAt(5, 0.502f),
        frameAt(6, 0.499f),
        frameAt(7, 0.500f),
        frameAt(8, 0.501f),
    )

    // Same cadence; hip/ankle Y show a clear rise-then-fall pattern
    // (image-space Y decreases as the body moves up), distinguishable from
    // idle at a glance. Not functionally interpreted this phase (no
    // detector exists yet) — a regression fixture for Phases 6/7 to reuse.
    val movementFrames: List<SkeletonFrame> = listOf(
        frameAt(0, 0.50f, 0.90f),
        frameAt(1, 0.47f, 0.88f),
        frameAt(2, 0.42f, 0.83f),
        frameAt(3, 0.38f, 0.78f),
        frameAt(4, 0.35f, 0.75f),
        frameAt(5, 0.38f, 0.78f),
        frameAt(6, 0.42f, 0.83f),
        frameAt(7, 0.47f, 0.88f),
        frameAt(8, 0.50f, 0.90f),
    )

    val idleFixtureJson: String = PoseJson.encodeToString(idleFrames)
    val movementFixtureJson: String = PoseJson.encodeToString(movementFrames)
}
