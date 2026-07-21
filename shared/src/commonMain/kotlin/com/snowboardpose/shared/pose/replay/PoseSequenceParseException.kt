package com.snowboardpose.shared.pose.replay

/**
 * Thrown for any pose sequence JSON that cannot be turned into a usable,
 * ordered [com.snowboardpose.shared.pose.SkeletonFrame] list: malformed
 * syntax, wrong field types, a missing required field (e.g.
 * `timestampMillis`), a non-array root, or an empty array. Extends
 * [IllegalArgumentException], matching this codebase's existing
 * validate-via-`require()` convention (see
 * [com.snowboardpose.shared.pose.Joint], [com.snowboardpose.shared.pose.MovementEvent]).
 */
class PoseSequenceParseException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
