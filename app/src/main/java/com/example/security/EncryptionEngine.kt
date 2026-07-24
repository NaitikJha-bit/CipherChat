package com.example.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedPayload(
    val ciphertext: String,
    val iv: String,
    val keyFingerprint: String
)

object EncryptionEngine {

    private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE_BYTES = 12
    private const val MASTER_SALT = "CipherChatE2EE_2026_Salt"

    /**
     * Derives a 256-bit AES key from a chat channel ID or user passphrase
     */
    fun deriveSharedKey(channelSeed: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest((channelSeed + MASTER_SALT).toByteArray(Charsets.UTF_8))
        return SecretKeySpec(hash, "AES")
    }

    /**
     * Encrypts plaintext using AES-256-GCM
     */
    fun encrypt(plaintext: String, channelSeed: String): EncryptedPayload {
        val secretKey = deriveSharedKey(channelSeed)
        val iv = ByteArray(IV_SIZE_BYTES)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val ciphertextBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val keyFingerprint = generateSafetyNumbers(channelSeed)

        return EncryptedPayload(
            ciphertext = ciphertextBase64,
            iv = ivBase64,
            keyFingerprint = keyFingerprint
        )
    }

    /**
     * Decrypts ciphertext using AES-256-GCM
     */
    fun decrypt(ciphertextBase64: String, ivBase64: String, channelSeed: String): String {
        return try {
            val secretKey = deriveSharedKey(channelSeed)
            val encryptedBytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Decryption Failed: Invalid Key or Tampered Payload]"
        }
    }

    /**
     * Generates a 60-digit safety code (formatted in 12 blocks of 5 digits) for key verification
     */
    fun generateSafetyNumbers(seed: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hash = digest.digest(seed.toByteArray(Charsets.UTF_8))
        
        val digits = StringBuilder()
        for (i in 0 until 30) {
            val byteVal = (hash[i % hash.size].toInt() and 0xFF)
            val num = (byteVal * 100) % 100000
            digits.append(String.format("%05d", num))
            if (digits.length >= 60) break
        }

        val result = StringBuilder()
        val fullStr = digits.take(60)
        for (i in fullStr.indices step 5) {
            result.append(fullStr.substring(i, (i + 5).coerceAtMost(fullStr.length)))
            if (i + 5 < fullStr.length) {
                if ((i / 5 + 1) % 3 == 0) {
                    result.append("\n")
                } else {
                    result.append("  ")
                }
            }
        }
        return result.toString()
    }

    /**
     * Generates a short key fingerprint hash (e.g., "ED89-4B1A-99F2")
     */
    fun getShortFingerprint(seed: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(seed.toByteArray(Charsets.UTF_8))
        val hex = hash.joinToString("") { "%02X".format(it) }
        return "${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}"
    }
}
