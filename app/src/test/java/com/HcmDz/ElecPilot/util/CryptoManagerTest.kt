package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.AEADBadTagException

class CryptoManagerTest {

    private fun newKey(): SecretKey =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun roundTrip() {
        val key = newKey()
        for (plaintext in listOf(
            "hello",
            "",
            "tokens with émojis 🔐 and quotes \" \n\t and trailing spaces   ",
            "{\"access_token\":\"ya29.a0x\",\"refresh_token\":\"1//abc\"}"
        )) {
            val stored = CryptoManager.encrypt(plaintext, key)
            assertNotEquals(plaintext, stored)
            assertEquals(plaintext, CryptoManager.decrypt(stored, key))
        }
    }

    @Test
    fun wrongKeyFails() {
        val stored = CryptoManager.encrypt("secret-token", newKey())
        assertThrows(AEADBadTagException::class.java) {
            CryptoManager.decrypt(stored, newKey())
        }
    }

    @Test
    fun malformedPayloadFails() {
        assertThrows(IllegalArgumentException::class.java) {
            CryptoManager.decrypt("not-a-payload", newKey())
        }
    }

    @Test
    fun encryptionIsRandomized() {
        val key = newKey()
        val a = CryptoManager.encrypt("same", key)
        val b = CryptoManager.encrypt("same", key)
        assertNotEquals(a, b)
    }
}
