package com.snowboardpose.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingTest {

    @Test
    fun sharedGreeting_returnsExpectedFixedValue() {
        assertEquals("Hello from shared Kotlin Multiplatform code", sharedGreeting())
    }
}
