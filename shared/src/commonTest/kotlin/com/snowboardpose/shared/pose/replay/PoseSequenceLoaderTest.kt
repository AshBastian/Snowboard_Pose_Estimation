package com.snowboardpose.shared.pose.replay

import com.snowboardpose.shared.pose.JointType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PoseSequenceLoaderTest {

    @Test
    fun loadJson_validJsonArray_decodesAllFrames() {
        val loaded = PoseSequenceLoader.loadJson(PoseFixtures.idleFixtureJson)

        assertEquals(PoseFixtures.idleFrames, loaded)
    }

    @Test
    fun loadJson_framesOutOfOrderInInput_returnsAscendingByTimestamp() {
        val outOfOrderJson = """
            [
              {"timestampMillis":66,"joints":{},"worldJoints":{}},
              {"timestampMillis":0,"joints":{},"worldJoints":{}},
              {"timestampMillis":33,"joints":{},"worldJoints":{}}
            ]
        """.trimIndent()

        val loaded = PoseSequenceLoader.loadJson(outOfOrderJson)

        assertEquals(listOf(0L, 33L, 66L), loaded.map { it.timestampMillis })
    }

    @Test
    fun loadJson_duplicateTimestamps_preservesStableRelativeOrder() {
        val duplicateTimestampJson = """
            [
              {"timestampMillis":10,"joints":{"NOSE":{"x":0.1,"y":0.1,"z":0.0,"visibility":0.9}},"worldJoints":{}},
              {"timestampMillis":10,"joints":{"NOSE":{"x":0.2,"y":0.2,"z":0.0,"visibility":0.9}},"worldJoints":{}}
            ]
        """.trimIndent()

        val loaded = PoseSequenceLoader.loadJson(duplicateTimestampJson)

        assertEquals(2, loaded.size)
        assertEquals(10L, loaded[0].timestampMillis)
        assertEquals(10L, loaded[1].timestampMillis)
        assertEquals(0.1f, loaded[0].joint(JointType.NOSE)?.x)
        assertEquals(0.2f, loaded[1].joint(JointType.NOSE)?.x)
    }

    @Test
    fun loadJson_missingTimestampKey_throwsPoseSequenceParseException() {
        val missingTimestampJson = """[{"joints":{},"worldJoints":{}}]"""

        assertFailsWith<PoseSequenceParseException> {
            PoseSequenceLoader.loadJson(missingTimestampJson)
        }
    }

    @Test
    fun loadJson_malformedJsonSyntax_throwsPoseSequenceParseException() {
        val brokenJson = """[{"timestampMillis":0,"joints":{}"""

        assertFailsWith<PoseSequenceParseException> {
            PoseSequenceLoader.loadJson(brokenJson)
        }
    }

    @Test
    fun loadJson_wrongFieldType_throwsPoseSequenceParseException() {
        val wrongTypeJson = """[{"timestampMillis":"zero","joints":{},"worldJoints":{}}]"""

        assertFailsWith<PoseSequenceParseException> {
            PoseSequenceLoader.loadJson(wrongTypeJson)
        }
    }

    @Test
    fun loadJson_nonArrayRoot_throwsPoseSequenceParseException() {
        val objectRootJson = """{"timestampMillis":0,"joints":{},"worldJoints":{}}"""

        assertFailsWith<PoseSequenceParseException> {
            PoseSequenceLoader.loadJson(objectRootJson)
        }
    }

    @Test
    fun loadJson_emptyArray_throwsPoseSequenceParseException() {
        val exception = assertFailsWith<PoseSequenceParseException> {
            PoseSequenceLoader.loadJson("[]")
        }
        assertTrue(exception.message!!.contains("at least one frame"))
    }
}
