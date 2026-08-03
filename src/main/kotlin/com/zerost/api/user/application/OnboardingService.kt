package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.shop.domain.ShopRepository
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.dto.CompleteOnboardingResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

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

        log.info(
            "onboarding_started userId={} nickname={} shopId={}",
            requireNotNull(user.id),
            command.nickname,
            command.shopId,
        )

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

        val userId = requireNotNull(user.id)
        val nickname = requireNotNull(user.nickname)
        val shopId = requireNotNull(user.shop?.id)
        val phoneNumber = maskPhoneNumber(requireNotNull(user.phoneNumber))
        registerAfterCommitLog {
            log.info(
                "onboarding_completed userId={} nickname={} shopId={} phoneNumber={}",
                userId,
                nickname,
                shopId,
                phoneNumber,
            )
        }

        return CompleteOnboardingResponse(
            userId = userId,
            nickname = nickname,
            phoneNumber = requireNotNull(user.phoneNumber),
            shopId = shopId,
        )
    }

    private fun validateNickname(userId: Long, nickname: String) {
        val existingUser = userRepository.findByNickname(nickname).orElse(null) ?: return
        if (existingUser.id != userId) {
            log.warn(
                "onboarding_failed reason=duplicate_nickname userId={} nickname={} existingUserId={}",
                userId,
                nickname,
                requireNotNull(existingUser.id),
            )
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }

    private fun validatePhoneNumber(userId: Long, phoneNumber: String) {
        val existingUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null) ?: return
        if (existingUser.id != userId) {
            log.warn(
                "onboarding_failed reason=duplicate_phone_number userId={} phoneNumber={} existingUserId={}",
                userId,
                maskPhoneNumber(phoneNumber),
                requireNotNull(existingUser.id),
            )
            throw BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER)
        }
    }

    private fun maskPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length < 4) {
            return "***"
        }
        return "${phoneNumber.take(3)}-****-${phoneNumber.takeLast(4)}"
    }

    private fun registerAfterCommitLog(action: () -> Unit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(OnboardingService::class.java)
    }
}
