package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.PoseJson
import com.snowboardpose.shared.pose.SkeletonFrame
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

/**
 * Parses a recorded pose sequence from JSON text (format: [PoseJson]) into
 * an ordered [List] of [SkeletonFrame]. Pure function — no file/disk/network
 * IO; getting JSON text into memory is the caller's job (tests now, a
 * future Phase 12 file-import adapter later).
 */
object PoseSequenceLoader {

    fun loadJson(text: String): List<SkeletonFrame> {
        val frames = try {
            PoseJson.decodeFromString<List<SkeletonFrame>>(text)
        } catch (e: SerializationException) {
            throw PoseSequenceParseException("Malformed pose sequence JSON: ${e.message}", e)
        }
        if (frames.isEmpty()) {
            throw PoseSequenceParseException("Pose sequence must contain at least one frame")
        }
        // Stable sort: input need not be pre-sorted. Frames with equal
        // timestampMillis are NOT rejected — they keep their original
        // relative order. Real-world duplicate timestamps are a valid
        // input that downstream detectors (Phase 5) are responsible for
        // tolerating; this loader's job is deterministic ordering, not
        // policy.
        return frames.sortedBy { it.timestampMillis }
    }
}
