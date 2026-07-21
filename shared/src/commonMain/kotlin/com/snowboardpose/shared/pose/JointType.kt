package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

/**
 * MediaPipe Pose Landmarker's fixed 33-landmark set, in MediaPipe's own index order
 * (ordinal 0 = NOSE ... ordinal 32 = RIGHT_FOOT_INDEX). This order is load-bearing:
 * later phases (Android/iOS MediaPipe adapters) map MediaPipe's raw landmark index
 * array onto this enum 1:1 by ordinal. Do not reorder existing entries.
 */
@Serializable
enum class JointType {
    NOSE,
    LEFT_EYE_INNER,
    LEFT_EYE,
    LEFT_EYE_OUTER,
    RIGHT_EYE_INNER,
    RIGHT_EYE,
    RIGHT_EYE_OUTER,
    LEFT_EAR,
    RIGHT_EAR,
    MOUTH_LEFT,
    MOUTH_RIGHT,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_PINKY,
    RIGHT_PINKY,
    LEFT_INDEX,
    RIGHT_INDEX,
    LEFT_THUMB,
    RIGHT_THUMB,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX,
}
