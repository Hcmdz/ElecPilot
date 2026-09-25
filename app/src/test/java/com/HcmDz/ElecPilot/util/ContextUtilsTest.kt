package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ContextUtilsTest {

    @Test
    fun `stored language wins over the system language`() {
        assertEquals("fr", resolveLanguage("fr", "en"))
        assertEquals("ar", resolveLanguage("ar", "fr"))
    }

    @Test
    fun `system resolves to a supported system language`() {
        assertEquals("fr", resolveLanguage("system", "fr"))
        assertEquals("en", resolveLanguage("system", "en"))
        assertEquals("ar", resolveLanguage("system", "ar"))
    }

    @Test
    fun `unsupported system language falls back to english`() {
        assertEquals("en", resolveLanguage("system", "es"))
        assertEquals("en", resolveLanguage("system", "de"))
        assertEquals("en", resolveLanguage("system", ""))
    }
}
