package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JointTest {

    @Test
    fun joint_storesValuesCorrectly() {
        val joint = Joint(x = 0.25f, y = 0.5f, z = -0.1f, visibility = 0.9f)
        assertEquals(0.25f, joint.x)
        assertEquals(0.5f, joint.y)
        assertEquals(-0.1f, joint.z)
        assertEquals(0.9f, joint.visibility)
    }

    @Test
    fun joint_acceptsVisibilityAtLowerBound() {
        Joint(x = 0f, y = 0f, z = 0f, visibility = 0f)
    }

    @Test
    fun joint_acceptsVisibilityAtUpperBound() {
        Joint(x = 0f, y = 0f, z = 0f, visibility = 1f)
    }

    @Test
    fun joint_rejectsVisibilityBelowZero() {
        assertFailsWith<IllegalArgumentException> {
            Joint(x = 0f, y = 0f, z = 0f, visibility = -0.01f)
        }
    }

    @Test
    fun joint_rejectsVisibilityAboveOne() {
        assertFailsWith<IllegalArgumentException> {
            Joint(x = 0f, y = 0f, z = 0f, visibility = 1.01f)
        }
    }

    @Test
    fun joint_equalInstancesAreEqual() {
        val a = Joint(x = 0.1f, y = 0.2f, z = 0.3f, visibility = 0.4f)
        val b = Joint(x = 0.1f, y = 0.2f, z = 0.3f, visibility = 0.4f)
        assertEquals(a, b)
    }

    @Test
    fun joint_copyChangesOnlyTargetedField() {
        val original = Joint(x = 0.1f, y = 0.2f, z = 0.3f, visibility = 0.4f)
        val copy = original.copy(visibility = 0.8f)
        assertEquals(original.x, copy.x)
        assertEquals(original.y, copy.y)
        assertEquals(original.z, copy.z)
        assertEquals(0.8f, copy.visibility)
    }
}
