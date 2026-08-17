package com.HcmDz.ElecPilot.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val KEY_ALIAS = "elecpilot_master_key"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128

    fun ensureKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) return
        try {
            generateKey(strongBox = true)
        } catch (e: StrongBoxUnavailableException) {
            generateKey(strongBox = false)
        }
    }

    private fun generateKey(strongBox: Boolean) {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setRandomizedEncryptionRequired(true)
            if (strongBox) setIsStrongBoxBacked(true)
        }.build()
        keyGen.init(spec)
        keyGen.generateKey()
    }

    fun encrypt(plaintext: String): String {
        ensureKey()
        return encrypt(plaintext, getSecretKey())
    }

    fun decrypt(stored: String): String = decrypt(stored, getSecretKey())

    fun encrypt(plaintext: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return "${Base64.getEncoder().encodeToString(cipher.iv)}:${Base64.getEncoder().encodeToString(ciphertext)}"
    }

    fun decrypt(stored: String, key: SecretKey): String {
        val parts = stored.split(":", limit = 2)
        require(parts.size == 2) { "Invalid encrypted payload" }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(TAG_LENGTH_BITS, Base64.getDecoder().decode(parts[0]))
        )
        return String(cipher.doFinal(Base64.getDecoder().decode(parts[1])), Charsets.UTF_8)
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
}
