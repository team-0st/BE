package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.presentation.dto.GachaHistoryResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GachaQueryService(
    private val userRepository: UserRepository,
    private val gachaRepository: GachaRepository,
) {

    @Transactional(readOnly = true)
    fun getGachaHistories(deviceId: String): List<GachaHistoryResponse> {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return gachaRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(requireNotNull(user.id))
            .map { gacha ->
                GachaHistoryResponse(
                    gachaId = requireNotNull(gacha.id),
                    costEcoJam = gacha.costEcoJam,
                    resultType = gacha.resultType.name,
                    resultPoint = gacha.resultPoint,
                    resultEcoJam = gacha.resultEcoJam,
                    resultIngredientId = gacha.resultIngredient?.id,
                    resultIngredientName = gacha.resultIngredient?.name,
                    resultIngredientQuantity = gacha.resultIngredientQuantity,
                    createdAt = gacha.createdAt.toString(),
                )
            }
    }
}
