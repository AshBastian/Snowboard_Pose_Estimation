package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.MovementEvent
import com.snowboardpose.shared.pose.SkeletonFrame

/**
 * Placeholder processing-pipeline contract standing in for the real
 * normalization/filtering/detection pipeline Phases 4-7 build. Given the
 * current frame and the immediately preceding one (`null` on the first
 * frame), returns a [MovementEvent] if this frame produced one.
 */
fun interface PoseFrameSink {
    fun onFrame(frame: SkeletonFrame, previous: SkeletonFrame?): MovementEvent?
}
