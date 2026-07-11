package com.example.utils

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    /**
     * Generates a 32-character hexadecimal random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes the given password with the salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = password + salt
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the entered password hashes to the expected string when combined with the salt.
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash == expectedHash
    }
}
