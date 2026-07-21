package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SkeletonFrameTest {

    private val hip = Joint(x = 0.5f, y = 0.5f, z = 0f, visibility = 0.95f)

    @Test
    fun joint_returnsStoredJoint() {
        val frame = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        assertEquals(hip, frame.joint(JointType.LEFT_HIP))
    }

    @Test
    fun joint_returnsNullForAbsentType() {
        val frame = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        assertNull(frame.joint(JointType.RIGHT_HIP))
    }

    @Test
    fun hasJoint_falseForAbsentType() {
        val frame = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        assertTrue(frame.hasJoint(JointType.LEFT_HIP))
        assertFalse(frame.hasJoint(JointType.RIGHT_HIP))
    }

    @Test
    fun worldJoints_defaultsToEmpty() {
        val frame = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        assertEquals(emptyMap(), frame.worldJoints)
        assertNull(frame.worldJoint(JointType.LEFT_HIP))
    }

    @Test
    fun imageAndWorldMaps_areIndependent() {
        val worldHip = Joint(x = 0.1f, y = -0.2f, z = 0.3f, visibility = 0.9f)
        val frame = SkeletonFrame(
            timestampMillis = 1000L,
            joints = mapOf(JointType.LEFT_HIP to hip),
            worldJoints = mapOf(JointType.LEFT_HIP to worldHip),
        )
        assertEquals(hip, frame.joint(JointType.LEFT_HIP))
        assertEquals(worldHip, frame.worldJoint(JointType.LEFT_HIP))
    }

    @Test
    fun equalInstancesAreEqual() {
        val a = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        val b = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        assertEquals(a, b)
    }

    @Test
    fun copyChangesOnlyTargetedField() {
        val original = SkeletonFrame(timestampMillis = 1000L, joints = mapOf(JointType.LEFT_HIP to hip))
        val copy = original.copy(timestampMillis = 2000L)
        assertEquals(original.joints, copy.joints)
        assertEquals(2000L, copy.timestampMillis)
    }
}
