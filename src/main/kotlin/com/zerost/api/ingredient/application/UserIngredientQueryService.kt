package com.zerost.api.ingredient.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.ingredient.presentation.dto.UserIngredientResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserIngredientQueryService(
    private val userRepository: UserRepository,
    private val userIngredientRepository: UserIngredientRepository
) {

    @Transactional(readOnly = true)
    fun getUserIngredients(deviceId: String): List<UserIngredientResponse> {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return userIngredientRepository.findAllByUserIdOrderByIdAsc(requireNotNull(user.id))
            .map{ userIngredient ->
                UserIngredientResponse(
                    ingredientId = requireNotNull(userIngredient.ingredient.id),
                    name = userIngredient.ingredient.name,
                    type = userIngredient.ingredient.type.name,
                    imageUrl = userIngredient.ingredient.imageUrl,
                    quantity = userIngredient.quantity,
                )
            }
    }
}
