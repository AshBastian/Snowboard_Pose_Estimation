package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

/**
 * A single tracked landmark. When stored in [SkeletonFrame.joints], [x]/[y] are
 * normalized image coordinates; when stored in [SkeletonFrame.worldJoints], they
 * are metric, hip-centered world coordinates. [z] follows the same convention as
 * whichever map holds this instance.
 */
@Serializable
data class Joint(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float,
) {
    init {
        require(visibility in 0f..1f) { "visibility must be in [0,1], was $visibility" }
    }
}
