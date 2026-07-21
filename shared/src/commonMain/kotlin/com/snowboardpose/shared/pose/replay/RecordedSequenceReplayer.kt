package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.SkeletonFrame

/**
 * Deterministically replays an already-ordered frame sequence through a
 * [PoseFrameSink]: plain synchronous iteration, no timers, no wall-clock,
 * no coroutines — two replays of the same input always produce identical
 * output.
 */
object RecordedSequenceReplayer {

    fun replay(frames: List<SkeletonFrame>, sink: PoseFrameSink): List<MovementEvent> {
        val events = mutableListOf<MovementEvent>()
        var previous: SkeletonFrame? = null
        for (frame in frames) {
            sink.onFrame(frame, previous)?.let(events::add)
            previous = frame
        }
        return events
    }
}
