package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.JointType
import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.MovementType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Phase 3 integration/system-test coverage for the master plan's "recorded
 * file loads into SkeletonFrame objects", "loaded sequence passes through a
 * placeholder pipeline", "expected number of frames reaches the detector",
 * and "output events are captured correctly" requirements.
 *
 * This also satisfies the master plan's Phase 3 system-test bullet
 * ("command-line or test-runner workflow processes a complete fixture; a
 * known fixture produces a known output summary") via the "test-runner
 * workflow" alternative it explicitly allows: this repo has no CLI module
 * (`settings.gradle.kts` includes only `:shared`/`:androidApp`), so this
 * test — running under both `:shared:jvmTest` and
 * `:shared:testAndroidHostTest` — is the test-runner workflow, the same
 * mapping technique Phase 2 used for its Android-adapter integration test.
 */
class PoseSequenceReplayIntegrationTest {

    @Test
    fun idleFixtureJson_loadsIntoSkeletonFrames() {
        val loaded = PoseSequenceLoader.loadJson(PoseFixtures.idleFixtureJson)

        assertEquals(PoseFixtures.idleFrames, loaded)
    }

    @Test
    fun loadedSequence_replayedThroughPlaceholderPipeline_reachesExpectedFrameCount() {
        val loaded = PoseSequenceLoader.loadJson(PoseFixtures.idleFixtureJson)

        var frameCount = 0
        RecordedSequenceReplayer.replay(loaded) { _, _ ->
            frameCount++
            null
        }

        assertEquals(PoseFixtures.idleFrames.size, frameCount)
    }

    @Test
    fun knownMovementFixture_producesKnownOutputSummary() {
        val loaded = PoseSequenceLoader.loadJson(PoseFixtures.movementFixtureJson)

        // Placeholder detector: emits one HOP event on the frame with the
        // lowest hip Y (the "peak" of the synthetic movement fixture);
        // only non-null events are collected, so this is a fixed, known
        // summary for a known fixture.
        val peakTimestamp = loaded.minByOrNull { frame ->
            frame.joint(JointType.LEFT_HIP)?.y ?: Float.MAX_VALUE
        }!!.timestampMillis

        val events = RecordedSequenceReplayer.replay(loaded) { frame, _ ->
            if (frame.timestampMillis == peakTimestamp) {
                MovementEvent(type = MovementType.HOP, timestampMillis = frame.timestampMillis, confidence = 1f)
            } else {
                null
            }
        }

        assertEquals(1, events.size)
        assertEquals(MovementType.HOP, events.single().type)
        assertEquals(peakTimestamp, events.single().timestampMillis)
    }
}
