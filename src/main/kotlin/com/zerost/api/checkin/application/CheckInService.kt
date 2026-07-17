package com.zerost.api.checkin.application

import com.zerost.api.checkin.domain.CheckIn
import com.zerost.api.checkin.domain.CheckInRepository
import com.zerost.api.checkin.presentation.dto.CheckInResponse
import com.zerost.api.checkin.presentation.dto.CheckInStatusResponse
import com.zerost.api.checkin.presentation.dto.RewardedIngredientResponse
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CheckInService(
    private val userRepository: UserRepository,
    private val ingredientRepository: IngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val checkInRepository: CheckInRepository,
) {

    @Transactional
    fun checkIn(deviceId: String): CheckInResponse {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow{ BusinessException(ErrorCode.USER_NOT_FOUND) }

        val today = LocalDate.now()
        if (checkInRepository.existsByUserIdAndCheckedDate(requireNotNull(user.id), today)) {
            throw BusinessException(ErrorCode.ALREADY_CHECKED_IN)
        }

        // 추후 랜덤으로 변경 예정
        val rewardedIngredient = ingredientRepository.findFirstByOrderByIdAsc()
            ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

        val userIngredient = userIngredientRepository.findByUserAndIngredient(user, rewardedIngredient)
            .orElseGet {
                UserIngredient(
                    user = user,
                    ingredient = rewardedIngredient,
                    quantity = 0
                )
            }

        userIngredient.increaseQuantity()
        userIngredientRepository.save(userIngredient)

        checkInRepository.save(
            CheckIn(
                user = user,
                rewardedIngredient = rewardedIngredient,
                checkedDate = today,
            )
        )

        return CheckInResponse(
            rewardedIngredient = RewardedIngredientResponse(
                id = requireNotNull(rewardedIngredient.id),
                name = rewardedIngredient.name,
                type = rewardedIngredient.type.name,
                imageUrl = rewardedIngredient.imageUrl,
            )
        )
    }

    @Transactional(readOnly = true)
    fun getTodayStatus(deviceId: String): CheckInStatusResponse {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val checkedIn = checkInRepository.existsByUserIdAndCheckedDate(requireNotNull(user.id), LocalDate.now())

        return CheckInStatusResponse(checkedIn = checkedIn)
    }
}
