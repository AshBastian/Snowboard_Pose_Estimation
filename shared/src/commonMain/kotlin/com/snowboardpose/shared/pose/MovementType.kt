package com.snowboardpose.shared.pose

import kotlinx.serialization.Serializable

@Serializable
enum class MovementType {
    NONE,
    HOP,
    TURN_AROUND,
}
