package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.shop.domain.ShopRepository
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.dto.CompleteOnboardingResponse
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingService(
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun complete(command: CompleteOnboardingCommand): CompleteOnboardingResponse {
        val user = userRepository.findById(command.userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val shop = shopRepository.findById(command.shopId)
            .orElseThrow { BusinessException(ErrorCode.SHOP_NOT_FOUND) }

        validateNickname(userId = requireNotNull(user.id), nickname = command.nickname)
        validatePhoneNumber(userId = requireNotNull(user.id), phoneNumber = command.phoneNumber)

        val encodedPassword = requireNotNull(passwordEncoder.encode(command.password))

        user.completeOnboarding(
            nickname = command.nickname,
            phoneNumber = command.phoneNumber,
            passwordHash = encodedPassword,
            shop = shop,
        )

        return CompleteOnboardingResponse(
            userId = requireNotNull(user.id),
            nickname = requireNotNull(user.nickname),
            phoneNumber = requireNotNull(user.phoneNumber),
            shopId = requireNotNull(user.shop?.id),
        )
    }

    private fun validateNickname(userId: Long, nickname: String) {
        val existingUser = userRepository.findByNickname(nickname).orElse(null) ?: return
        if (existingUser.id != userId) {
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }

    private fun validatePhoneNumber(userId: Long, phoneNumber: String) {
        val existingUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null) ?: return
        if (existingUser.id != userId) {
            throw BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER)
        }
    }
}
