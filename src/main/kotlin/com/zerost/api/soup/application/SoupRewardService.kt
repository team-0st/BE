package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.common.reward.WeightedCandidate
import com.zerost.api.common.reward.WeightedRandomSelector
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientHistory
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistorySourceType
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.Soup
import com.zerost.api.soup.domain.SoupBonusRewardPolicy
import com.zerost.api.soup.domain.SoupBonusRewardPolicyIngredient
import com.zerost.api.soup.domain.SoupBonusRewardPolicyIngredientRepository
import com.zerost.api.soup.domain.SoupBonusRewardPolicyRepository
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRewardIngredient
import com.zerost.api.soup.domain.SoupRewardIngredientRepository
import com.zerost.api.soup.domain.SoupRewardIngredientSelectionType
import com.zerost.api.soup.domain.SoupRewardPolicy
import com.zerost.api.soup.domain.SoupRewardPolicyIngredient
import com.zerost.api.soup.domain.SoupRewardPolicyIngredientRepository
import com.zerost.api.soup.domain.SoupRewardPolicyRepository
import com.zerost.api.soup.domain.SoupRerollPolicyCandidate
import com.zerost.api.soup.domain.SoupRerollPolicyCandidateRepository
import com.zerost.api.soup.domain.SoupRerollPolicyGroup
import com.zerost.api.soup.domain.SoupRerollPolicyGroupRepository
import com.zerost.api.soup.domain.SoupRerollPolicyIngredient
import com.zerost.api.soup.domain.SoupRerollPolicyIngredientRepository
import com.zerost.api.soup.presentation.dto.SoupRewardIngredientResponse
import com.zerost.api.soup.presentation.dto.SoupRewardSectionResponse
import com.zerost.api.soup.presentation.dto.SoupRewardSummary
import java.math.BigDecimal
import org.springframework.stereotype.Service

@Service
class SoupRewardService(
    private val ingredientRepository: IngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val soupRewardIngredientRepository: SoupRewardIngredientRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val soupRewardPolicyRepository: SoupRewardPolicyRepository,
    private val soupRewardPolicyIngredientRepository: SoupRewardPolicyIngredientRepository,
    private val soupBonusRewardPolicyRepository: SoupBonusRewardPolicyRepository,
    private val soupBonusRewardPolicyIngredientRepository: SoupBonusRewardPolicyIngredientRepository,
    private val soupRerollPolicyGroupRepository: SoupRerollPolicyGroupRepository,
    private val soupRerollPolicyCandidateRepository: SoupRerollPolicyCandidateRepository,
    private val soupRerollPolicyIngredientRepository: SoupRerollPolicyIngredientRepository,
    private val randomProvider: RandomProvider,
) {

    companion object {
        private val PROBABILITY_SCALE = BigDecimal("100")
        private val BONUS_REWARD_RECIPE_TYPES = setOf(RecipeType.HIDDEN, RecipeType.LEGENDARY)
    }

    fun reward(soup: Soup): SoupRewardSummary {
        val baseReward = loadPolicyBasedReward(soup)
        val bonusReward = loadBonusRewardOrNull(soup)
        val reward = mergeRewards(baseReward, bonusReward)

        applyReward(
            soup = soup,
            reward = reward,
            baseReward = baseReward,
            bonusReward = bonusReward,
        )

        return SoupRewardSummary(
            rewardGrade = reward.rewardGrade.name,
            ecoJam = reward.ecoJam,
            point = reward.point,
            rewardedIngredients = reward.rewardedIngredients.map { ingredientReward ->
                SoupRewardIngredientResponse(
                    ingredientId = requireNotNull(ingredientReward.ingredient.id),
                    ingredientName = ingredientReward.ingredient.name,
                    quantity = ingredientReward.quantity,
                )
            },
            baseReward = baseReward.toSectionResponse(),
            bonusReward = bonusReward?.toSectionResponse(),
        )
    }

    fun reroll(soup: Soup): SoupRewardSummary {
        revokeReward(soup)

        val reward = loadRerollPolicyBasedReward(soup)

        applyReward(
            soup = soup,
            reward = reward,
            baseReward = reward,
            bonusReward = null,
            ecoJamHistorySourceType = EcoJamHistorySourceType.SOUP_REROLL,
            pointHistorySourceType = PointHistorySourceType.SOUP_REROLL,
            ingredientHistorySourceType = IngredientHistorySourceType.SOUP_REROLL,
        )

        return SoupRewardSummary(
            rewardGrade = reward.rewardGrade.name,
            ecoJam = reward.ecoJam,
            point = reward.point,
            rewardedIngredients = reward.rewardedIngredients.map { ingredientReward ->
                SoupRewardIngredientResponse(
                    ingredientId = requireNotNull(ingredientReward.ingredient.id),
                    ingredientName = ingredientReward.ingredient.name,
                    quantity = ingredientReward.quantity,
                )
            },
            baseReward = reward.toSectionResponse(),
        )
    }

    fun validateRewardRecoverable(soup: Soup) {
        if (soup.user.ecoJam < soup.rewardEcoJam || soup.user.point < soup.rewardPoint) {
            throw BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE)
        }

        val rewardedIngredients = soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(requireNotNull(soup.id))
        rewardedIngredients.forEach { rewardedIngredient ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, rewardedIngredient.ingredient)
                .orElseThrow { BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE) }

            if (userIngredient.quantity < rewardedIngredient.quantity) {
                throw BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE)
            }
        }
    }

    private fun loadPolicyBasedReward(soup: Soup): RewardResult {
        val policies = soupRewardPolicyRepository.findAllByRecipeTypeAndIntroOnlyAndActiveTrueOrderByIdAsc(
            recipeType = soup.recipe.type,
            introOnly = soup.recipe.intro,
        )
        if (policies.isEmpty()) {
            throw BusinessException(ErrorCode.SOUP_REWARD_POLICY_NOT_FOUND)
        }

        val policyIds = policies.map { requireNotNull(it.id) }
        val ingredientsByPolicyId = soupRewardPolicyIngredientRepository
            .findAllBySoupRewardPolicyIdInOrderByIdAsc(policyIds)
            .groupBy { requireNotNull(it.soupRewardPolicy.id) }

        val selectedPolicy = selectPolicy(policies)
        val rewardedIngredients = resolveRewardedIngredients(
            ingredients = ingredientsByPolicyId[requireNotNull(selectedPolicy.id)].orEmpty(),
        )

        return RewardResult(
            rewardGrade = selectedPolicy.rewardGrade,
            ecoJam = selectedPolicy.ecoJamAmount,
            point = selectedPolicy.pointAmount,
            rewardedIngredients = rewardedIngredients,
        )
    }

    private fun loadRerollPolicyBasedReward(soup: Soup): RewardResult {
        val group = soupRerollPolicyGroupRepository.findByRecipeTypeAndCurrentRewardGradeAndActiveTrue(
            recipeType = soup.recipe.type,
            currentRewardGrade = soup.rewardGrade,
        ).orElseThrow { BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE) }

        val candidates = soupRerollPolicyCandidateRepository
            .findAllBySoupRerollPolicyGroupIdAndActiveTrueOrderByIdAsc(requireNotNull(group.id))
        if (candidates.isEmpty()) {
            throw BusinessException(ErrorCode.SOUP_REROLL_POLICY_NOT_FOUND)
        }

        val candidateIds = candidates.map { requireNotNull(it.id) }
        val ingredientsByCandidateId = soupRerollPolicyIngredientRepository
            .findAllBySoupRerollPolicyCandidateIdInOrderByIdAsc(candidateIds)
            .groupBy { requireNotNull(it.soupRerollPolicyCandidate.id) }

        val selectedCandidate = selectRerollCandidate(candidates)
        val rewardedIngredients = resolveRerollRewardedIngredients(
            ingredients = ingredientsByCandidateId[requireNotNull(selectedCandidate.id)].orEmpty(),
        )

        return RewardResult(
            rewardGrade = selectedCandidate.nextRewardGrade,
            ecoJam = selectedCandidate.ecoJamAmount,
            point = selectedCandidate.pointAmount,
            rewardedIngredients = rewardedIngredients,
        )
    }

    private fun loadBonusRewardOrNull(soup: Soup): RewardResult? {
        if (soup.recipe.intro || soup.recipe.type !in BONUS_REWARD_RECIPE_TYPES) {
            return null
        }

        val policies = soupBonusRewardPolicyRepository.findAllByRecipeTypeAndActiveTrueOrderByIdAsc(soup.recipe.type)
        if (policies.isEmpty()) {
            return null
        }

        val policyIds = policies.map { requireNotNull(it.id) }
        val ingredientsByPolicyId = soupBonusRewardPolicyIngredientRepository
            .findAllBySoupBonusRewardPolicyIdInOrderByIdAsc(policyIds)
            .groupBy { requireNotNull(it.soupBonusRewardPolicy.id) }

        val selectedPolicy = selectBonusPolicy(policies)
        val rewardedIngredients = resolveBonusRewardedIngredients(
            ingredients = ingredientsByPolicyId[requireNotNull(selectedPolicy.id)].orEmpty(),
        )

        return RewardResult(
            rewardGrade = selectedPolicy.rewardGrade,
            ecoJam = selectedPolicy.ecoJamAmount,
            point = selectedPolicy.pointAmount,
            rewardedIngredients = rewardedIngredients,
        )
    }

    private fun applyReward(
        soup: Soup,
        reward: RewardResult,
        baseReward: RewardResult,
        bonusReward: RewardResult? = null,
        ecoJamHistorySourceType: EcoJamHistorySourceType = EcoJamHistorySourceType.SOUP,
        pointHistorySourceType: PointHistorySourceType = PointHistorySourceType.SOUP,
        ingredientHistorySourceType: IngredientHistorySourceType = IngredientHistorySourceType.SOUP,
    ) {
        soup.rewardGrade = reward.rewardGrade
        soup.rewardEcoJam = reward.ecoJam
        soup.rewardPoint = reward.point
        soup.baseRewardGrade = baseReward.rewardGrade
        soup.baseRewardEcoJam = baseReward.ecoJam
        soup.baseRewardPoint = baseReward.point
        soup.bonusRewardGrade = bonusReward?.rewardGrade
        soup.bonusRewardEcoJam = bonusReward?.ecoJam ?: 0
        soup.bonusRewardPoint = bonusReward?.point ?: 0

        soup.user.increaseEcoJam(reward.ecoJam)
        soup.user.increasePoint(reward.point)
        saveHistories(soup, reward, ecoJamHistorySourceType, pointHistorySourceType)

        reward.rewardedIngredients.forEach { ingredientReward ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, ingredientReward.ingredient)
                .orElseGet {
                    UserIngredient(
                        user = soup.user,
                        ingredient = ingredientReward.ingredient,
                        quantity = 0,
                    )
                }

            userIngredient.increaseQuantity(ingredientReward.quantity)
            userIngredientRepository.save(userIngredient)

            ingredientHistoryRepository.save(
                IngredientHistory.earn(
                    user = soup.user,
                    ingredient = ingredientReward.ingredient,
                    amount = ingredientReward.quantity,
                    sourceType = ingredientHistorySourceType,
                    sourceId = requireNotNull(soup.id),
                ),
            )

            soupRewardIngredientRepository.save(
                SoupRewardIngredient(
                    soup = soup,
                    ingredient = ingredientReward.ingredient,
                    quantity = ingredientReward.quantity,
                ),
            )
        }
    }

    private fun revokeReward(soup: Soup) {
        val soupId = requireNotNull(soup.id)

        if (soup.rewardEcoJam > 0) {
            soup.user.decreaseEcoJam(soup.rewardEcoJam)
            ecoJamHistoryRepository.save(
                EcoJamHistory.spend(
                    user = soup.user,
                    amount = soup.rewardEcoJam,
                    sourceType = EcoJamHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }

        if (soup.rewardPoint > 0) {
            soup.user.decreasePoint(soup.rewardPoint)
            pointHistoryRepository.save(
                PointHistory.spend(
                    user = soup.user,
                    amount = soup.rewardPoint,
                    sourceType = PointHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }

        val rewardedIngredients = soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(soupId)
        rewardedIngredients.forEach { rewardedIngredient ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, rewardedIngredient.ingredient)
                .orElseThrow { BusinessException(ErrorCode.INSUFFICIENT_INGREDIENT_QUANTITY) }
            userIngredient.decreaseQuantity(rewardedIngredient.quantity)
            ingredientHistoryRepository.save(
                IngredientHistory.spend(
                    user = soup.user,
                    ingredient = rewardedIngredient.ingredient,
                    amount = rewardedIngredient.quantity,
                    sourceType = IngredientHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }
        soupRewardIngredientRepository.deleteAll(rewardedIngredients)
    }

    private fun saveHistories(
        soup: Soup,
        reward: RewardResult,
        ecoJamHistorySourceType: EcoJamHistorySourceType,
        pointHistorySourceType: PointHistorySourceType,
    ) {
        val soupId = requireNotNull(soup.id)

        if (reward.ecoJam > 0) {
            ecoJamHistoryRepository.save(
                EcoJamHistory.earn(
                    user = soup.user,
                    amount = reward.ecoJam,
                    sourceType = ecoJamHistorySourceType,
                    sourceId = soupId,
                ),
            )
        }

        if (reward.point > 0) {
            pointHistoryRepository.save(
                PointHistory.earn(
                    user = soup.user,
                    amount = reward.point,
                    sourceType = pointHistorySourceType,
                    sourceId = soupId,
                ),
            )
        }
    }

    private fun randomCommonIngredient(): Ingredient = ingredientRepository.findAllByType(IngredientType.COMMON)
        .takeIf { it.isNotEmpty() }
        ?.let { ingredients -> ingredients[randomProvider.nextInt(ingredients.size)] }
        ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

    private fun randomHiddenIngredient(): Ingredient = ingredientRepository.findAllByType(IngredientType.HIDDEN)
        .takeIf { it.isNotEmpty() }
        ?.let { ingredients -> ingredients[randomProvider.nextInt(ingredients.size)] }
        ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

    private fun randomIngredientByType(ingredientType: IngredientType): Ingredient = ingredientRepository.findAllByType(ingredientType)
        .takeIf { it.isNotEmpty() }
        ?.let { ingredients -> ingredients[randomProvider.nextInt(ingredients.size)] }
        ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

    private fun resolveRewardedIngredients(ingredients: List<SoupRewardPolicyIngredient>): List<IngredientReward> {
        if (ingredients.isEmpty()) {
            return emptyList()
        }

        val rewardedIngredientMap = linkedMapOf<Long, IngredientReward>()

        ingredients.forEach { ingredientPolicy ->
            when (ingredientPolicy.selectionType) {
                SoupRewardIngredientSelectionType.FIXED -> {
                    val ingredient = ingredientPolicy.ingredient
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
                    accumulateIngredientReward(rewardedIngredientMap, ingredient, ingredientPolicy.quantity)
                }

                SoupRewardIngredientSelectionType.RANDOM_BY_TYPE -> {
                    val ingredientType = ingredientPolicy.ingredientType
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
                    repeat(ingredientPolicy.quantity) {
                        val ingredient = randomIngredientByType(ingredientType)
                        accumulateIngredientReward(rewardedIngredientMap, ingredient, 1)
                    }
                }
            }
        }

        return rewardedIngredientMap.values.toList()
    }

    private fun resolveRerollRewardedIngredients(ingredients: List<SoupRerollPolicyIngredient>): List<IngredientReward> {
        if (ingredients.isEmpty()) {
            return emptyList()
        }

        val rewardedIngredientMap = linkedMapOf<Long, IngredientReward>()

        ingredients.forEach { ingredientPolicy ->
            when (ingredientPolicy.selectionType) {
                SoupRewardIngredientSelectionType.FIXED -> {
                    val ingredient = ingredientPolicy.ingredient
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REROLL_POLICY)
                    accumulateIngredientReward(rewardedIngredientMap, ingredient, ingredientPolicy.quantity)
                }

                SoupRewardIngredientSelectionType.RANDOM_BY_TYPE -> {
                    val ingredientType = ingredientPolicy.ingredientType
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REROLL_POLICY)
                    repeat(ingredientPolicy.quantity) {
                        val ingredient = randomIngredientByType(ingredientType)
                        accumulateIngredientReward(rewardedIngredientMap, ingredient, 1)
                    }
                }
            }
        }

        return rewardedIngredientMap.values.toList()
    }

    private fun resolveBonusRewardedIngredients(ingredients: List<SoupBonusRewardPolicyIngredient>): List<IngredientReward> {
        if (ingredients.isEmpty()) {
            return emptyList()
        }

        val rewardedIngredientMap = linkedMapOf<Long, IngredientReward>()

        ingredients.forEach { ingredientPolicy ->
            when (ingredientPolicy.selectionType) {
                SoupRewardIngredientSelectionType.FIXED -> {
                    val ingredient = ingredientPolicy.ingredient
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
                    accumulateIngredientReward(rewardedIngredientMap, ingredient, ingredientPolicy.quantity)
                }

                SoupRewardIngredientSelectionType.RANDOM_BY_TYPE -> {
                    val ingredientType = ingredientPolicy.ingredientType
                        ?: throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
                    repeat(ingredientPolicy.quantity) {
                        val ingredient = randomIngredientByType(ingredientType)
                        accumulateIngredientReward(rewardedIngredientMap, ingredient, 1)
                    }
                }
            }
        }

        return rewardedIngredientMap.values.toList()
    }

    private fun accumulateIngredientReward(
        rewardedIngredientMap: MutableMap<Long, IngredientReward>,
        ingredient: Ingredient,
        quantity: Int,
    ) {
        val ingredientId = requireNotNull(ingredient.id)
        val existingReward = rewardedIngredientMap[ingredientId]
        if (existingReward == null) {
            rewardedIngredientMap[ingredientId] = IngredientReward(ingredient = ingredient, quantity = quantity)
            return
        }

        rewardedIngredientMap[ingredientId] = existingReward.copy(quantity = existingReward.quantity + quantity)
    }

    private fun rewardCandidate(
        weight: Int,
        reward: () -> RewardResult,
    ): WeightedCandidate<() -> RewardResult> = WeightedCandidate(
        value = reward,
        weight = weight,
    )

    private fun selectReward(vararg candidates: WeightedCandidate<() -> RewardResult>): RewardResult =
        WeightedRandomSelector.select(candidates.toList()) { totalWeight ->
            randomProvider.nextInt(totalWeight)
        }.invoke()

    private fun selectPolicy(policies: List<SoupRewardPolicy>): SoupRewardPolicy {
        val weightedPolicies = policies.map { policy ->
            val weight = policy.probability.multiply(PROBABILITY_SCALE).toInt()
            if (weight <= 0) {
                throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
            }
            WeightedCandidate(value = policy, weight = weight)
        }

        return WeightedRandomSelector.select(weightedPolicies) { totalWeight ->
            randomProvider.nextInt(totalWeight)
        }
    }

    private fun selectBonusPolicy(policies: List<SoupBonusRewardPolicy>): SoupBonusRewardPolicy {
        val weightedPolicies = policies.map { policy ->
            val weight = policy.probability.multiply(PROBABILITY_SCALE).toInt()
            if (weight <= 0) {
                throw BusinessException(ErrorCode.INVALID_SOUP_REWARD_POLICY)
            }
            WeightedCandidate(value = policy, weight = weight)
        }

        return WeightedRandomSelector.select(weightedPolicies) { totalWeight ->
            randomProvider.nextInt(totalWeight)
        }
    }

    private fun selectRerollCandidate(candidates: List<SoupRerollPolicyCandidate>): SoupRerollPolicyCandidate {
        val weightedCandidates = candidates.map { candidate ->
            val weight = candidate.probability.multiply(PROBABILITY_SCALE).toInt()
            if (weight <= 0) {
                throw BusinessException(ErrorCode.INVALID_SOUP_REROLL_POLICY)
            }
            WeightedCandidate(value = candidate, weight = weight)
        }

        return WeightedRandomSelector.select(weightedCandidates) { totalWeight ->
            randomProvider.nextInt(totalWeight)
        }
    }

    private data class RewardResult(
        val rewardGrade: SoupRewardGrade,
        val ecoJam: Int = 0,
        val point: Int = 0,
        val rewardedIngredients: List<IngredientReward> = emptyList(),
    ) {
        fun toSectionResponse(): SoupRewardSectionResponse =
            SoupRewardSectionResponse(
                rewardGrade = rewardGrade.name,
                ecoJam = ecoJam,
                point = point,
                rewardedIngredients = rewardedIngredients.map { ingredientReward ->
                    SoupRewardIngredientResponse(
                        ingredientId = requireNotNull(ingredientReward.ingredient.id),
                        ingredientName = ingredientReward.ingredient.name,
                        quantity = ingredientReward.quantity,
                    )
                },
            )
    }

    private data class IngredientReward(
        val ingredient: Ingredient,
        val quantity: Int,
    )

    private fun mergeRewards(baseReward: RewardResult, bonusReward: RewardResult?): RewardResult {
        if (bonusReward == null) {
            return baseReward
        }

        val rewardedIngredientMap = linkedMapOf<Long, IngredientReward>()
        (baseReward.rewardedIngredients + bonusReward.rewardedIngredients).forEach { ingredientReward ->
            accumulateIngredientReward(rewardedIngredientMap, ingredientReward.ingredient, ingredientReward.quantity)
        }

        return RewardResult(
            rewardGrade = bonusReward.rewardGrade,
            ecoJam = baseReward.ecoJam + bonusReward.ecoJam,
            point = baseReward.point + bonusReward.point,
            rewardedIngredients = rewardedIngredientMap.values.toList(),
        )
    }

}
