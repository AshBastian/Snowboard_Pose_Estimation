package com.snowboardpose.shared.pose

import kotlinx.serialization.json.Json

/**
 * The single shared JSON format for all pose-model serialization (recorded
 * fixtures, Phase 3 replay files, Phase 12 exports).
 *
 * Format guarantees:
 * - Enums ([JointType], [MovementType]) serialize by stable name, never ordinal.
 * - `Map<JointType, Joint>` fields (e.g. [SkeletonFrame.joints]) serialize as a
 *   JSON object keyed by enum name. JSON object key order is NOT part of the
 *   format guarantee — do not rely on iteration order when reading raw JSON.
 * - Unknown keys in input JSON are ignored (forward-compatible with fields
 *   added by later phases) rather than failing deserialization.
 * - Fields with defaults are always written on encode, so recorded fixtures
 *   are self-describing and do not depend on the reader's default values.
 */
val PoseJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
