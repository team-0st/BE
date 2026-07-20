package com.zerost.api.mypage.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mypage.presentation.dto.MyPageIngredientResponse
import com.zerost.api.mypage.presentation.dto.MyPageResponse
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyPageQueryService(
    private val userRepository: UserRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
    private val soupRepository: SoupRepository,
) {

    @Transactional(readOnly = true)
    fun getMyPage(deviceId: String): MyPageResponse {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val userId = requireNotNull(user.id)
        val ingredients = userIngredientRepository.findAllByUserIdOrderByIdAsc(userId)
            .map { userIngredient ->
                MyPageIngredientResponse(
                    ingredientId = requireNotNull(userIngredient.ingredient.id),
                    name = userIngredient.ingredient.name,
                    type = userIngredient.ingredient.type.name,
                    imageUrl = userIngredient.ingredient.imageUrl,
                    quantity = userIngredient.quantity,
                )
            }

        return MyPageResponse(
            nickname = user.nickname,
            shopName = user.shop?.name,
            ecoJam = user.ecoJam,
            point = user.point,
            brewedSoupCount = soupRepository.countByUserId(userId).toInt(),
            completedMissionCount = missionCompletionRepository.countByUserIdAndStatus(
                userId = userId,
                status = MissionCompletionStatus.APPROVED,
            ).toInt(),
            totalIngredientQuantity = ingredients.sumOf { it.quantity },
            ingredients = ingredients,
        )
    }
}
