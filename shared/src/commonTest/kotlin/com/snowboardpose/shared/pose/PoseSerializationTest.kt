package com.snowboardpose.shared.pose

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class PoseSerializationTest {

    @Test
    fun joint_roundTripPreservesAllFields() {
        val joint = Joint(x = 0.12f, y = 0.34f, z = -0.56f, visibility = 0.78f)
        val encoded = PoseJson.encodeToString(joint)
        val decoded = PoseJson.decodeFromString<Joint>(encoded)
        assertEquals(joint, decoded)
    }

    @Test
    fun skeletonFrame_roundTripPreservesAllFields_includingWorldJoints() {
        val frame = SkeletonFrame(
            timestampMillis = 12345L,
            joints = mapOf(JointType.LEFT_HIP to Joint(0.1f, 0.2f, 0.3f, 0.9f)),
            worldJoints = mapOf(JointType.LEFT_HIP to Joint(-0.05f, 0.1f, 0.5f, 0.9f)),
        )
        val encoded = PoseJson.encodeToString(frame)
        val decoded = PoseJson.decodeFromString<SkeletonFrame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun skeletonFrame_roundTripPreservesEmptyMaps() {
        val frame = SkeletonFrame(timestampMillis = 1L)
        val encoded = PoseJson.encodeToString(frame)
        val decoded = PoseJson.decodeFromString<SkeletonFrame>(encoded)
        assertEquals(frame, decoded)
        assertEquals(emptyMap(), decoded.joints)
        assertEquals(emptyMap(), decoded.worldJoints)
    }

    @Test
    fun movementEvent_roundTripPreservesAllFields() {
        val event = MovementEvent(type = MovementType.HOP, timestampMillis = 999L, confidence = 0.42f)
        val encoded = PoseJson.encodeToString(event)
        val decoded = PoseJson.decodeFromString<MovementEvent>(encoded)
        assertEquals(event, decoded)
    }

    @Test
    fun poseProcessingConfig_roundTripPreservesAllFields() {
        val config = PoseProcessingConfig(visibilityThreshold = 0.6f, smoothingAlpha = 0.2f)
        val encoded = PoseJson.encodeToString(config)
        val decoded = PoseJson.decodeFromString<PoseProcessingConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun unknownJsonKey_doesNotThrow() {
        val jointObject = buildJsonObject {
            put("x", 0.1f)
            put("y", 0.2f)
            put("z", 0.3f)
            put("visibility", 0.5f)
            put("futureField", "ignored")
        }
        val decoded = PoseJson.decodeFromJsonElement<Joint>(jointObject)
        assertEquals(Joint(0.1f, 0.2f, 0.3f, 0.5f), decoded)
    }

    @Test
    fun missingOptionalField_fallsBackToDefault() {
        val frameObjectWithoutWorldJoints = buildJsonObject {
            put("timestampMillis", 1000)
            put("joints", buildJsonObject { })
        }
        val decoded = PoseJson.decodeFromJsonElement<SkeletonFrame>(frameObjectWithoutWorldJoints)
        assertEquals(emptyMap(), decoded.worldJoints)
    }

    @Test
    fun jointType_serializesByStableNameForAllEntries() {
        for (jointType in JointType.entries) {
            val encoded = PoseJson.encodeToString(jointType)
            assertEquals("\"${jointType.name}\"", encoded)
            assertEquals(jointType, PoseJson.decodeFromString<JointType>(encoded))
        }
    }

    @Test
    fun movementType_serializesByStableNameForAllEntries() {
        for (movementType in MovementType.entries) {
            val encoded = PoseJson.encodeToString(movementType)
            assertEquals("\"${movementType.name}\"", encoded)
            assertEquals(movementType, PoseJson.decodeFromString<MovementType>(encoded))
        }
    }
}
