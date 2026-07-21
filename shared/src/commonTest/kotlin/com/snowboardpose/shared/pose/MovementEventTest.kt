package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MovementEventTest {

    @Test
    fun movementEvent_acceptsConfidenceAtLowerBound() {
        MovementEvent(type = MovementType.HOP, timestampMillis = 1000L, confidence = 0f)
    }

    @Test
    fun movementEvent_acceptsConfidenceAtUpperBound() {
        MovementEvent(type = MovementType.HOP, timestampMillis = 1000L, confidence = 1f)
    }

    @Test
    fun movementEvent_rejectsConfidenceBelowZero() {
        assertFailsWith<IllegalArgumentException> {
            MovementEvent(type = MovementType.HOP, timestampMillis = 1000L, confidence = -0.01f)
        }
    }

    @Test
    fun movementEvent_rejectsConfidenceAboveOne() {
        assertFailsWith<IllegalArgumentException> {
            MovementEvent(type = MovementType.HOP, timestampMillis = 1000L, confidence = 1.01f)
        }
    }

    @Test
    fun movementEvent_equalInstancesAreEqual() {
        val a = MovementEvent(type = MovementType.TURN_AROUND, timestampMillis = 500L, confidence = 0.75f)
        val b = MovementEvent(type = MovementType.TURN_AROUND, timestampMillis = 500L, confidence = 0.75f)
        assertEquals(a, b)
    }

    @Test
    fun movementEvent_copyChangesOnlyTargetedField() {
        val original = MovementEvent(type = MovementType.HOP, timestampMillis = 500L, confidence = 0.75f)
        val copy = original.copy(confidence = 0.9f)
        assertEquals(original.type, copy.type)
        assertEquals(original.timestampMillis, copy.timestampMillis)
        assertEquals(0.9f, copy.confidence)
    }
}
