package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.MovementType
import com.snowboardpose.shared.pose.SkeletonFrame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordedSequenceReplayerTest {

    @Test
    fun replay_deliversFramesToSinkInOrder() {
        val visited = mutableListOf<SkeletonFrame>()
        RecordedSequenceReplayer.replay(PoseFixtures.idleFrames) { frame, _ ->
            visited += frame
            null
        }

        assertEquals(PoseFixtures.idleFrames, visited)
    }

    @Test
    fun replay_firstFrame_previousIsNull() {
        var firstCallPrevious: SkeletonFrame? = SkeletonFrame(timestampMillis = -1L)
        var callCount = 0
        RecordedSequenceReplayer.replay(PoseFixtures.idleFrames) { _, previous ->
            if (callCount == 0) firstCallPrevious = previous
            callCount++
            null
        }

        assertEquals(null, firstCallPrevious)
    }

    @Test
    fun replay_subsequentFrames_previousIsPriorFrame() {
        val previousSeen = mutableListOf<SkeletonFrame?>()
        RecordedSequenceReplayer.replay(PoseFixtures.idleFrames) { _, previous ->
            previousSeen += previous
            null
        }

        for (i in 1 until PoseFixtures.idleFrames.size) {
            assertEquals(PoseFixtures.idleFrames[i - 1], previousSeen[i])
        }
    }

    @Test
    fun replay_collectsOnlyNonNullSinkEvents() {
        val events = RecordedSequenceReplayer.replay(PoseFixtures.idleFrames) { frame, _ ->
            if (frame.timestampMillis == PoseFixtures.idleFrames.last().timestampMillis) {
                MovementEvent(type = MovementType.NONE, timestampMillis = frame.timestampMillis, confidence = 1f)
            } else {
                null
            }
        }

        assertEquals(1, events.size)
        assertEquals(PoseFixtures.idleFrames.last().timestampMillis, events.single().timestampMillis)
    }

    @Test
    fun replay_isDeterministic_repeatedRunsProduceIdenticalOutput() {
        fun sink(frame: SkeletonFrame, previous: SkeletonFrame?): MovementEvent? =
            if (previous != null && frame.timestampMillis - previous.timestampMillis == 0L) {
                MovementEvent(type = MovementType.NONE, timestampMillis = frame.timestampMillis, confidence = 0f)
            } else {
                null
            }

        val firstRun = RecordedSequenceReplayer.replay(PoseFixtures.movementFrames, ::sink)
        val secondRun = RecordedSequenceReplayer.replay(PoseFixtures.movementFrames, ::sink)

        assertEquals(firstRun, secondRun)
    }

    @Test
    fun replay_emptyFrameList_returnsEmptyEventsWithoutInvokingSink() {
        val events = RecordedSequenceReplayer.replay(emptyList()) { _, _ ->
            throw AssertionError("sink must not be invoked for an empty frame list")
        }

        assertTrue(events.isEmpty())
    }

    @Test
    fun replay_frameCountDeliveredMatchesInputSize() {
        var callCount = 0
        RecordedSequenceReplayer.replay(PoseFixtures.movementFrames) { _, _ ->
            callCount++
            null
        }

        assertEquals(PoseFixtures.movementFrames.size, callCount)
    }
}
