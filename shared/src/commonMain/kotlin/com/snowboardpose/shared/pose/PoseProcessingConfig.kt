package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

/**
 * Narrowly-scoped processing configuration for Phase 4 (normalization) and
 * Phase 5 (filtering). [visibilityThreshold] and [smoothingAlpha] defaults
 * are unvalidated placeholders, not empirically tuned values — Phase 4/5
 * are expected to consume/override them once real fixtures exist. Hop- and
 * turn-specific thresholds are intentionally NOT included here; they are
 * added in Phase 6/7 once there is an empirical basis for real numbers.
 */
@Serializable
data class PoseProcessingConfig(
    val visibilityThreshold: Float = 0.5f,
    val smoothingAlpha: Float = 0.3f,
) {
    init {
        require(visibilityThreshold in 0f..1f) {
            "visibilityThreshold must be in [0,1], was $visibilityThreshold"
        }
        require(smoothingAlpha in 0f..1f) {
            "smoothingAlpha must be in [0,1], was $smoothingAlpha"
        }
    }
}
