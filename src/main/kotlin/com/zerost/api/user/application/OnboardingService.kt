package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.shop.domain.ShopRepository
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.dto.CompleteOnboardingResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingService(
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
) {

    @Transactional
    fun complete(command: CompleteOnboardingCommand): CompleteOnboardingResponse {
        val user = userRepository.findByDeviceId(command.deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val shop = shopRepository.findById(command.shopId)
            .orElseThrow { BusinessException(ErrorCode.SHOP_NOT_FOUND) }

        user.completeOnboarding(
            nickname = command.nickname,
            phoneNumber = command.phoneNumber,
            shop = shop,
        )

        return CompleteOnboardingResponse(
            userId = requireNotNull(user.id),
            nickname = requireNotNull(user.nickname),
            phoneNumber = requireNotNull(user.phoneNumber),
            shopId = requireNotNull(user.shop?.id),
        )
    }
}
