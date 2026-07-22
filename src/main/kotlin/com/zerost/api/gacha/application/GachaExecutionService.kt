package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.common.reward.WeightedCandidate
import com.zerost.api.common.reward.WeightedRandomSelector
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.gacha.domain.Gacha
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.domain.GachaRewardPolicy
import com.zerost.api.gacha.domain.GachaRewardPolicyRepository
import com.zerost.api.gacha.presentation.dto.ExecuteGachaResponse
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientHistory
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientHistorySourceType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class GachaExecutionService(
    private val userRepository: UserRepository,
    private val gachaRewardPolicyRepository: GachaRewardPolicyRepository,
    private val gachaRepository: GachaRepository,
    private val ingredientRepository: IngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val gachaRandomProvider: GachaRandomProvider,
) {

    @Transactional
    fun execute(userId: Long): ExecuteGachaResponse {
        val user = userRepository.findByIdForUpdate(userId)
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
        val resolvedReward = resolveReward(selectedPolicy)

        user.decreaseEcoJam(costEcoJam)

        val gacha = gachaRepository.save(
            Gacha(
                user = user,
                rewardPolicy = selectedPolicy,
                costEcoJam = costEcoJam,
                resultType = selectedPolicy.rewardType,
                resultPoint = selectedPolicy.pointAmount,
                resultEcoJam = selectedPolicy.ecoJamAmount,
                resultIngredient = resolvedReward.ingredient,
                resultIngredientQuantity = resolvedReward.ingredientQuantity,
            ),
        )

        applyReward(user, gacha, requireNotNull(gacha.id))
        saveHistories(user, gacha)

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
        val weightedPolicies = policies.mapNotNull { policy ->
            val weight = policy.probability.multiply(PROBABILITY_SCALE).toInt()
            if (weight > 0) {
                WeightedCandidate(value = policy, weight = weight)
            } else {
                null
            }
        }

        if (weightedPolicies.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_GACHA_REWARD_POLICY)
        }

        return WeightedRandomSelector.select(weightedPolicies) { totalWeight ->
            gachaRandomProvider.nextInt(totalWeight)
        }
    }

    private fun resolveReward(selectedPolicy: GachaRewardPolicy): ResolvedReward {
        if (selectedPolicy.rewardType != com.zerost.api.gacha.domain.GachaRewardType.INGREDIENT) {
            return ResolvedReward()
        }

        val fixedIngredient = selectedPolicy.ingredient
        if (fixedIngredient != null) {
            return ResolvedReward(
                ingredient = fixedIngredient,
                ingredientQuantity = selectedPolicy.ingredientQuantity,
            )
        }

        val ingredientType = selectedPolicy.ingredientType
            ?: throw BusinessException(ErrorCode.INVALID_GACHA_REWARD_POLICY)
        val candidates = ingredientRepository.findAllByType(ingredientType)
        if (candidates.isEmpty()) {
            throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)
        }

        val selectedIngredient = if (candidates.size == 1) {
            candidates.first()
        } else {
            candidates[gachaRandomProvider.nextInt(candidates.size)]
        }

        return ResolvedReward(
            ingredient = selectedIngredient,
            ingredientQuantity = selectedPolicy.ingredientQuantity,
        )
    }

    private fun applyReward(
        user: User,
        gacha: Gacha,
        gachaId: Long,
    ) {
        if (gacha.resultPoint > 0) {
            user.increasePoint(gacha.resultPoint)
        }

        if (gacha.resultEcoJam > 0) {
            user.increaseEcoJam(gacha.resultEcoJam)
        }

        val ingredient = gacha.resultIngredient
        if (ingredient != null && gacha.resultIngredientQuantity > 0) {
            val userIngredient = userIngredientRepository.findByUserAndIngredient(user, ingredient)
                .orElseGet {
                    UserIngredient(
                        user = user,
                        ingredient = ingredient,
                        quantity = 0,
                    )
                }

            userIngredient.increaseQuantity(gacha.resultIngredientQuantity)
            userIngredientRepository.save(userIngredient)
            ingredientHistoryRepository.save(
                IngredientHistory.earn(
                    user = user,
                    ingredient = ingredient,
                    amount = gacha.resultIngredientQuantity,
                    sourceType = IngredientHistorySourceType.GACHA,
                    sourceId = gachaId,
                ),
            )
        }
    }

    private fun saveHistories(
        user: User,
        gacha: Gacha
    ) {
        val gachaId = requireNotNull(gacha.id)

        ecoJamHistoryRepository.save(
            EcoJamHistory.spend(
                user = user,
                amount = gacha.costEcoJam,
                sourceType = EcoJamHistorySourceType.GACHA,
                sourceId = gachaId,
            )
        )

        if (gacha.resultEcoJam > 0) {
            ecoJamHistoryRepository.save(
                EcoJamHistory.earn(
                    user = user,
                    amount = gacha.resultEcoJam,
                    sourceType = EcoJamHistorySourceType.GACHA,
                    sourceId = gachaId,
                )
            )
        }

        if (gacha.resultPoint > 0) {
            pointHistoryRepository.save(
                PointHistory.earn(
                    user = user,
                    amount = gacha.resultPoint,
                    sourceType = PointHistorySourceType.GACHA,
                    sourceId = gachaId,
                )
            )
        }
    }

    companion object {
        private const val GACHA_COST_ECO_JAM = 100
        private val PROBABILITY_SCALE = BigDecimal("100")
    }

    private data class ResolvedReward(
        val ingredient: Ingredient? = null,
        val ingredientQuantity: Int = 0,
    )
}
