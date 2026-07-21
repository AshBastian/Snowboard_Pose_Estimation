package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

@Serializable
data class MovementEvent(
    val type: MovementType,
    val timestampMillis: Long,
    val confidence: Float,
) {
    init {
        require(confidence in 0f..1f) { "confidence must be in [0,1], was $confidence" }
    }
}
