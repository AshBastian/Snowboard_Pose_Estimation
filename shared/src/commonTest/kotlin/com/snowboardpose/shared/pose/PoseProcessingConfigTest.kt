package com.snowboardpose.shared.pose

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PoseProcessingConfigTest {

    @Test
    fun defaults_areWithinValidRange() {
        val config = PoseProcessingConfig()
        assertTrue(config.visibilityThreshold in 0f..1f)
        assertTrue(config.smoothingAlpha in 0f..1f)
    }

    @Test
    fun rejectsOutOfRangeVisibilityThreshold() {
        assertFailsWith<IllegalArgumentException> {
            PoseProcessingConfig(visibilityThreshold = 1.5f)
        }
    }

    @Test
    fun rejectsOutOfRangeSmoothingAlpha() {
        assertFailsWith<IllegalArgumentException> {
            PoseProcessingConfig(smoothingAlpha = -0.1f)
        }
    }

    @Test
    fun copy_reValidates() {
        val config = PoseProcessingConfig()
        assertFailsWith<IllegalArgumentException> {
            config.copy(visibilityThreshold = 2f)
        }
    }
}
