package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 2 integration coverage for the master plan's "recorded fixture loads
 * into shared models" and "Android adapter constructs a shared skeleton from
 * a fixture" requirements. There is no androidMain adapter code yet
 * (Milestone 3), so this test builds an in-memory fixture directly with the
 * shared models and runs under both `:shared:jvmTest` and
 * `:shared:testAndroidHostTest` — proving the models compose and construct
 * identically on both current targets without needing a separate androidMain
 * test file. A real JSON-text loader now exists at
 * [com.snowboardpose.shared.pose.replay.PoseSequenceLoader] (Phase 3); this
 * test still hand-builds its own in-memory fixture rather than using it, to
 * keep Phase 2's model-level coverage independent of Phase 3's parser.
 */
class PoseModelFixtureIntegrationTest {

    @Test
    fun handBuiltSequence_preservesFrameOrderingAndPerFrameLookups() {
        val hipRise = listOf(0.50f, 0.48f, 0.45f, 0.48f, 0.50f)
        val sequence = hipRise.mapIndexed { index, hipY ->
            SkeletonFrame(
                timestampMillis = index * 33L,
                joints = mapOf(
                    JointType.LEFT_HIP to Joint(0.45f, hipY, 0f, 0.95f),
                    JointType.RIGHT_HIP to Joint(0.55f, hipY, 0f, 0.95f),
                    JointType.LEFT_ANKLE to Joint(0.45f, 0.9f, 0f, 0.9f),
                    JointType.RIGHT_ANKLE to Joint(0.55f, 0.9f, 0f, 0.9f),
                ),
            )
        }

        assertEquals(5, sequence.size)
        // Frame ordering: timestamps strictly increase.
        for (i in 1 until sequence.size) {
            assertTrue(sequence[i].timestampMillis > sequence[i - 1].timestampMillis)
        }
        // Correct per-frame lookups.
        assertEquals(0.45f, sequence[2].joint(JointType.LEFT_HIP)?.y)
    }

    @Test
    fun handBuiltSequence_withMissingJoint_staysSafe() {
        val frameWithMissingAnkle = SkeletonFrame(
            timestampMillis = 100L,
            joints = mapOf(
                JointType.LEFT_HIP to Joint(0.45f, 0.5f, 0f, 0.95f),
                JointType.RIGHT_HIP to Joint(0.55f, 0.5f, 0f, 0.95f),
                // LEFT_ANKLE deliberately absent — simulates a brief landmark-loss frame.
                JointType.RIGHT_ANKLE to Joint(0.55f, 0.9f, 0f, 0.9f),
            ),
        )

        assertNull(frameWithMissingAnkle.joint(JointType.LEFT_ANKLE))
        assertTrue(frameWithMissingAnkle.hasJoint(JointType.RIGHT_ANKLE))
        assertEquals(3, frameWithMissingAnkle.joints.size)
    }

    @Test
    fun movementEvent_canBeConstructedFromSequenceDerivedData() {
        val sequence = listOf(
            SkeletonFrame(timestampMillis = 0L, joints = mapOf(JointType.LEFT_HIP to Joint(0.45f, 0.50f, 0f, 0.95f))),
            SkeletonFrame(timestampMillis = 33L, joints = mapOf(JointType.LEFT_HIP to Joint(0.45f, 0.40f, 0f, 0.95f))),
        )
        val lastFrame = sequence.last()
        val event = MovementEvent(type = MovementType.HOP, timestampMillis = lastFrame.timestampMillis, confidence = 0.8f)

        assertEquals(33L, event.timestampMillis)
        assertEquals(MovementType.HOP, event.type)
    }
}
