package com.zerost.api.user.application

import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.RegisterUserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun register(deviceId: String): RegisterUserResponse {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseGet {
                userRepository.save(User(deviceId = deviceId))
            }

        return RegisterUserResponse(
            userId = requireNotNull(user.id),
            onboardingCompleted = user.onboardingCompleted,
        )
    }
}
