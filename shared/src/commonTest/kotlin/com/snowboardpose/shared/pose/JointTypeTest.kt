package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertEquals

class JointTypeTest {

    @Test
    fun jointType_hasExactlyThirtyThreeEntries() {
        assertEquals(33, JointType.entries.size)
    }

    @Test
    fun jointType_ordinalOrder_matchesMediaPipeLandmarkIndexTable() {
        val expectedOrder = listOf(
            JointType.NOSE,
            JointType.LEFT_EYE_INNER,
            JointType.LEFT_EYE,
            JointType.LEFT_EYE_OUTER,
            JointType.RIGHT_EYE_INNER,
            JointType.RIGHT_EYE,
            JointType.RIGHT_EYE_OUTER,
            JointType.LEFT_EAR,
            JointType.RIGHT_EAR,
            JointType.MOUTH_LEFT,
            JointType.MOUTH_RIGHT,
            JointType.LEFT_SHOULDER,
            JointType.RIGHT_SHOULDER,
            JointType.LEFT_ELBOW,
            JointType.RIGHT_ELBOW,
            JointType.LEFT_WRIST,
            JointType.RIGHT_WRIST,
            JointType.LEFT_PINKY,
            JointType.RIGHT_PINKY,
            JointType.LEFT_INDEX,
            JointType.RIGHT_INDEX,
            JointType.LEFT_THUMB,
            JointType.RIGHT_THUMB,
            JointType.LEFT_HIP,
            JointType.RIGHT_HIP,
            JointType.LEFT_KNEE,
            JointType.RIGHT_KNEE,
            JointType.LEFT_ANKLE,
            JointType.RIGHT_ANKLE,
            JointType.LEFT_HEEL,
            JointType.RIGHT_HEEL,
            JointType.LEFT_FOOT_INDEX,
            JointType.RIGHT_FOOT_INDEX,
        )
        assertEquals(expectedOrder, JointType.entries.toList())
        expectedOrder.forEachIndexed { index, jointType ->
            assertEquals(index, jointType.ordinal, "unexpected ordinal for $jointType")
        }
    }
}
