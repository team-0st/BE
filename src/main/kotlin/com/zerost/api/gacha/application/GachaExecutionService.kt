package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.gacha.domain.Gacha
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.domain.GachaRewardPolicy
import com.zerost.api.gacha.domain.GachaRewardPolicyRepository
import com.zerost.api.gacha.presentation.dto.ExecuteGachaResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class GachaExecutionService(
    private val userRepository: UserRepository,
    private val gachaRewardPolicyRepository: GachaRewardPolicyRepository,
    private val gachaRepository: GachaRepository,
    private val gachaRandomProvider: GachaRandomProvider,
) {

    @Transactional
    fun execute(deviceId: String): ExecuteGachaResponse {
        val user = userRepository.findByDeviceIdForUpdate(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val costEcoJam = GACHA_COST_ECO_JAM
        if (user.ecoJam < costEcoJam) {
            throw BusinessException(ErrorCode.INSUFFICIENT_ECO_JAM)
        }

        val activePolicies = gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()
        if (activePolicies.isEmpty()) {
            throw BusinessException(ErrorCode.GACHA_REWARD_POLICY_NOT_FOUND)
        }

        val selectedPolicy = selectPolicy(activePolicies)

        user.decreaseEcoJam(costEcoJam)

        val gacha = gachaRepository.save(
            Gacha(
                user = user,
                rewardPolicy = selectedPolicy,
                costEcoJam = costEcoJam,
                resultType = selectedPolicy.rewardType,
                resultPoint = selectedPolicy.pointAmount,
                resultEcoJam = selectedPolicy.ecoJamAmount,
                resultIngredient = selectedPolicy.ingredient,
                resultIngredientQuantity = selectedPolicy.ingredientQuantity,
            ),
        )

        return ExecuteGachaResponse(
            gachaId = requireNotNull(gacha.id),
            costEcoJam = costEcoJam,
            remainingEcoJam = user.ecoJam,
            resultType = gacha.resultType.name,
            resultPoint = gacha.resultPoint,
            resultEcoJam = gacha.resultEcoJam,
            resultIngredientId = gacha.resultIngredient?.id,
            resultIngredientQuantity = gacha.resultIngredientQuantity,
        )
    }

    private fun selectPolicy(policies: List<GachaRewardPolicy>): GachaRewardPolicy {
        val weights = policies.map { policy ->
            val weight = policy.probability.multiply(PROBABILITY_SCALE).toInt()
            if (weight <= 0) {
                throw BusinessException(ErrorCode.INVALID_GACHA_REWARD_POLICY)
            }
            weight
        }

        val totalWeight = weights.sum()
        if (totalWeight <= 0) {
            throw BusinessException(ErrorCode.INVALID_GACHA_REWARD_POLICY)
        }

        val roll = gachaRandomProvider.nextInt(totalWeight)
        var cumulativeWeight = 0

        policies.forEachIndexed { index, policy ->
            cumulativeWeight += weights[index]
            if (roll < cumulativeWeight) {
                return policy
            }
        }

        throw BusinessException(ErrorCode.INVALID_GACHA_REWARD_POLICY)
    }

    companion object {
        private const val GACHA_COST_ECO_JAM = 100
        private val PROBABILITY_SCALE = BigDecimal("100")
    }
}
