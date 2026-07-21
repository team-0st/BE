package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthLoginService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional(readOnly = true)
    fun login(phoneNumber: String, password: String): LoginResponse {
        val user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow { BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS) }

        val passwordHash = user.passwordHash
        if (!user.onboardingCompleted || passwordHash.isNullOrBlank() || !passwordEncoder.matches(password, passwordHash)) {
            throw BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS)
        }

        return LoginResponse(
            userId = requireNotNull(user.id),
            deviceId = user.deviceId,
            nickname = requireNotNull(user.nickname),
            phoneNumber = requireNotNull(user.phoneNumber),
            onboardingCompleted = user.onboardingCompleted,
        )
    }
}
