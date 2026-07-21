package com.zerost.api.auth.application

import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class RefreshTokenHasher {

    fun hash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
