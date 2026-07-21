package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

/**
 * A single timestamped pose sample. [joints] holds normalized image-space
 * landmarks; [worldJoints] holds optional metric, hip-centered world-space
 * landmarks (empty when unavailable — "no world data" and "zero world joints"
 * are the same state, not a null/empty/present tri-state). A joint absent
 * from a map means that landmark was not detected for this frame.
 */
@Serializable
data class SkeletonFrame(
    val timestampMillis: Long,
    val joints: Map<JointType, Joint> = emptyMap(),
    val worldJoints: Map<JointType, Joint> = emptyMap(),
) {
    fun joint(type: JointType): Joint? = joints[type]

    fun worldJoint(type: JointType): Joint? = worldJoints[type]

    fun hasJoint(type: JointType): Boolean = joints.containsKey(type)
}
