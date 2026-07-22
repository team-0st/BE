package com.zerost.api.support

import com.zerost.api.auth.application.AccessTokenClaims
import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.shop.domain.Shop
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.communitymission.domain.CommunityMission
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.communitymission.domain.CommunityMissionProof
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirement
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.communitymission.domain.CommunityMissionReward
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCategory
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeIngredient
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipe
import com.zerost.api.soup.domain.Soup
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRewardIngredient
import com.zerost.api.soup.domain.SoupRewardIngredientSelectionType
import com.zerost.api.soup.domain.SoupRewardPolicy
import com.zerost.api.soup.domain.SoupRewardPolicyIngredient
import com.zerost.api.soup.domain.SoupRerollPolicyCandidate
import com.zerost.api.soup.domain.SoupRerollPolicyGroup
import com.zerost.api.soup.domain.SoupRerollPolicyIngredient
import com.zerost.api.user.application.CompleteOnboardingCommand
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRole
import java.math.BigDecimal
import java.time.LocalDateTime
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

fun createUser(
    id: Long? = 1L,
    @Suppress("UNUSED_PARAMETER")
    deviceId: String = "device-1",
    nickname: String? = null,
    phoneNumber: String? = null,
    passwordHash: String? = null,
    role: UserRole = UserRole.USER,
    profileCharacterCode: ProfileCharacterCode? = null,
    shop: Shop? = null,
    ecoJam: Int = 0,
    point: Int = 0,
    onboardingCompleted: Boolean = false,
): User = User(
    id = id,
    nickname = nickname,
    phoneNumber = phoneNumber,
    passwordHash = passwordHash,
    role = role,
    profileCharacterCode = profileCharacterCode,
    shop = shop,
    ecoJam = ecoJam,
    point = point,
    onboardingCompleted = onboardingCompleted,
)

fun createShop(
    id: Long = 1L,
    name: String = "알맹상점",
    description: String? = null,
    imageUrl: String? = null,
): Shop = Shop(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
)

fun createIngredient(
    id: Long = 1L,
    name: String = "버려진 천",
    type: IngredientType = IngredientType.COMMON,
    imageUrl: String? = null,
): Ingredient = Ingredient(
    id = id,
    name = name,
    type = type,
    imageUrl = imageUrl,
)

fun createUserIngredient(
    id: Long = 1L,
    user: User = createUser(),
    ingredient: Ingredient = createIngredient(),
    quantity: Int = 0,
): UserIngredient = UserIngredient(
    id = id,
    user = user,
    ingredient = ingredient,
    quantity = quantity,
)

fun createOnboardingCommand(
    userId: Long = 1L,
    nickname: String = "펭귄탐험가",
    phoneNumber: String = "010-1234-5678",
    password: String = "zerost1234",
    shopId: Long = 1L,
): CompleteOnboardingCommand = CompleteOnboardingCommand(
    userId = userId,
    nickname = nickname,
    phoneNumber = phoneNumber,
    password = password,
    shopId = shopId,
)

fun createRegisterUserRequestBody(): String = "{}"

fun createUpdateNicknameRequestBody(
    nickname: String = "펭귄탐험가",
): String = """
    {"nickname":"$nickname"}
""".trimIndent()

fun createOnboardingRequestBody(
    nickname: String = "펭귄탐험가",
    phoneNumber: String = "010-1234-5678",
    password: String = "zerost1234",
    shopId: Long? = 1L,
): String {
    val shopIdField = shopId?.let {
        """
          ,
          "shopId": $it
        """.trimIndent()
    } ?: ""
    return """
        {
          "nickname": "$nickname",
          "phoneNumber": "$phoneNumber",
          "password": "$password"$shopIdField
        }
    """.trimIndent()
}

fun createLoginRequestBody(
    phoneNumber: String = "010-1234-5678",
    password: String = "zerost1234",
): String = """
    {"phoneNumber":"$phoneNumber","password":"$password"}
""".trimIndent()

fun createRefreshTokenRequestBody(
    refreshToken: String = "refresh-token",
): String = """
    {"refreshToken":"$refreshToken"}
""".trimIndent()

fun createSubmitCommunityMissionProofRequestBody(
    photoKeys: List<String> = listOf("community-missions/1/1/2026/07/21/proof-1.jpg"),
): String = """
    {"photoKeys":[${photoKeys.joinToString(",") { "\"$it\"" }}]}
""".trimIndent()

fun createAuthTokenProvider(
    userId: Long = 1L,
    role: UserRole = UserRole.USER,
): AuthTokenProvider {
    val authTokenProvider = mock(AuthTokenProvider::class.java)
    `when`(authTokenProvider.parseAccessToken("access-token")).thenReturn(
        AccessTokenClaims(
            userId = userId,
            role = role,
        ),
    )
    return authTokenProvider
}

fun createMission(
    id: Long = 1L,
    title: String = "텀블러 사용하기",
    description: String? = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.",
    imageUrl: String? = "https://example.com/images/mission-1.png",
    missionCategory: MissionCategory = MissionCategory.GENERAL,
    rewardIngredientPool: String = "[1,2,3]",
): Mission = Mission(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    missionCategory = missionCategory,
    rewardIngredientPool = rewardIngredientPool,
)

fun createMissionCompletion(
    id: Long = 1L,
    user: User = createUser(),
    mission: Mission = createMission(),
    photoKey: String = "missions/1/1/2026/07/18/mission-1.jpg",
    status: MissionCompletionStatus = MissionCompletionStatus.PENDING,
    rewardedIngredient: Ingredient? = null,
    submittedAt: LocalDateTime = LocalDateTime.of(2026, 7, 17, 10, 0, 0),
    reviewedAt: LocalDateTime? = null,
    rewardClaimedAt: LocalDateTime? = null,
): MissionCompletion = MissionCompletion(
    id = id,
    user = user,
    mission = mission,
    photoKey = photoKey,
    status = status,
    rewardedIngredient = rewardedIngredient,
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
    rewardClaimedAt = rewardClaimedAt,
)

fun createCommunityMission(
    id: Long = 1L,
    title: String = "오늘의 친환경 약속",
    description: String? = "친구 또는 가족과 함께 오늘 실천할 친환경 행동 1가지를 정하고 함께 실천합니다.",
    difficulty: CommunityMissionDifficulty = CommunityMissionDifficulty.ONE_STAR,
    stage: Int = 1,
    targetRatio: BigDecimal = BigDecimal("30.00"),
    imageUrl: String? = "https://example.com/images/community-mission-1.png",
    active: Boolean = true,
    succeededAt: LocalDateTime? = null,
): CommunityMission = CommunityMission(
    id = id,
    title = title,
    description = description,
    difficulty = difficulty,
    stage = stage,
    targetRatio = targetRatio,
    imageUrl = imageUrl,
    active = active,
    succeededAt = succeededAt,
)

fun createCommunityMissionCompletion(
    id: Long = 1L,
    communityMission: CommunityMission = createCommunityMission(),
    user: User = createUser(),
    completedAt: LocalDateTime = LocalDateTime.of(2026, 7, 21, 10, 0, 0),
    rewardedAt: LocalDateTime? = null,
): CommunityMissionCompletion = CommunityMissionCompletion(
    id = id,
    communityMission = communityMission,
    user = user,
    completedAt = completedAt,
    rewardedAt = rewardedAt,
)

fun createCommunityMissionProofRequirement(
    id: Long? = 1L,
    communityMission: CommunityMission = createCommunityMission(),
    proofOrder: Int = 1,
    title: String? = "1일차 인증",
    description: String? = "사진 제출",
    requiredImageCount: Int = 1,
    requiredDayOffset: Int? = null,
): CommunityMissionProofRequirement = CommunityMissionProofRequirement(
    id = id,
    communityMission = communityMission,
    proofOrder = proofOrder,
    title = title,
    description = description,
    requiredImageCount = requiredImageCount,
    requiredDayOffset = requiredDayOffset,
)

fun createCommunityMissionProof(
    id: Long? = 1L,
    communityMission: CommunityMission = createCommunityMission(),
    proofRequirement: CommunityMissionProofRequirement = createCommunityMissionProofRequirement(communityMission = communityMission),
    user: User = createUser(),
    status: CommunityMissionProofStatus = CommunityMissionProofStatus.PENDING,
    submittedAt: LocalDateTime = LocalDateTime.of(2026, 7, 21, 10, 0, 0),
    reviewedAt: LocalDateTime? = null,
    imageKeys: List<String> = listOf("community-missions/1/1/2026/07/21/proof-1.jpg"),
): CommunityMissionProof {
    val proof = CommunityMissionProof(
        id = id,
        communityMission = communityMission,
        proofRequirement = proofRequirement,
        user = user,
        status = status,
        submittedAt = submittedAt,
        reviewedAt = reviewedAt,
    )
    imageKeys.forEachIndexed { index, imageKey ->
        proof.addImage(imageKey, index + 1)
    }
    return proof
}

fun createReviewCommunityMissionProofRequestBody(
    status: String = "APPROVED",
): String = """
    {"status":"$status"}
""".trimIndent()

fun createCommunityMissionReward(
    id: Long = 1L,
    communityMission: CommunityMission = createCommunityMission(),
    rewardType: CommunityMissionRewardType = CommunityMissionRewardType.ECO_JAM,
    ingredientType: IngredientType? = null,
    quantity: Int = 0,
    ecoJamAmount: Int = 50,
    rewardOrder: Int = 1,
): CommunityMissionReward = CommunityMissionReward(
    id = id,
    communityMission = communityMission,
    rewardType = rewardType,
    ingredientType = ingredientType,
    quantity = quantity,
    ecoJamAmount = ecoJamAmount,
    rewardOrder = rewardOrder,
)

fun createRecipe(
    id: Long = 1L,
    name: String = "오리지널 스프",
    type: RecipeType = RecipeType.COMMON,
    slotCount: Int = 3,
    intro: Boolean = false,
    weekly: Boolean = false,
    hidden: Boolean = false,
): Recipe = Recipe(
    id = id,
    name = name,
    type = type,
    slotCount = slotCount,
    intro = intro,
    weekly = weekly,
    hidden = hidden,
)

fun createRecipeIngredient(
    id: Long = 1L,
    recipe: Recipe = createRecipe(),
    ingredient: Ingredient = createIngredient(),
    slotOrder: Int = 1,
): RecipeIngredient = RecipeIngredient(
    id = id,
    recipe = recipe,
    ingredient = ingredient,
    slotOrder = slotOrder,
)

fun createUserUnlockedRecipe(
    id: Long = 1L,
    user: User = createUser(),
    recipe: Recipe = createRecipe(type = RecipeType.HIDDEN),
): UserUnlockedRecipe = UserUnlockedRecipe(
    id = id,
    user = user,
    recipe = recipe,
)

fun createSoup(
    id: Long = 1L,
    user: User = createUser(),
    recipe: Recipe = createRecipe(),
    rewardGrade: SoupRewardGrade = SoupRewardGrade.CONSOLATION,
    rewardEcoJam: Int = 0,
    rewardPoint: Int = 0,
    rerolled: Boolean = false,
): Soup = Soup(
    id = id,
    user = user,
    recipe = recipe,
    rewardGrade = rewardGrade,
    rewardEcoJam = rewardEcoJam,
    rewardPoint = rewardPoint,
    rerolled = rerolled,
)

fun createSoupRewardPolicy(
    id: Long = 1L,
    recipeType: RecipeType = RecipeType.COMMON,
    introOnly: Boolean = false,
    rewardGrade: SoupRewardGrade = SoupRewardGrade.JACKPOT,
    probability: BigDecimal = BigDecimal("100.00"),
    pointAmount: Int = 0,
    ecoJamAmount: Int = 0,
    active: Boolean = true,
): SoupRewardPolicy = SoupRewardPolicy(
    id = id,
    recipeType = recipeType,
    introOnly = introOnly,
    rewardGrade = rewardGrade,
    probability = probability,
    pointAmount = pointAmount,
    ecoJamAmount = ecoJamAmount,
    active = active,
)

fun createSoupRewardPolicyIngredient(
    id: Long = 1L,
    soupRewardPolicy: SoupRewardPolicy = createSoupRewardPolicy(),
    selectionType: SoupRewardIngredientSelectionType = SoupRewardIngredientSelectionType.RANDOM_BY_TYPE,
    ingredient: Ingredient? = null,
    ingredientType: IngredientType? = IngredientType.COMMON,
    quantity: Int = 1,
): SoupRewardPolicyIngredient = SoupRewardPolicyIngredient(
    id = id,
    soupRewardPolicy = soupRewardPolicy,
    selectionType = selectionType,
    ingredient = ingredient,
    ingredientType = ingredientType,
    quantity = quantity,
)

fun createSoupRerollPolicyGroup(
    id: Long = 1L,
    recipeType: RecipeType = RecipeType.COMMON,
    currentRewardGrade: SoupRewardGrade = SoupRewardGrade.CONSOLATION,
    rerollCostEcoJam: Int = 30,
    active: Boolean = true,
): SoupRerollPolicyGroup = SoupRerollPolicyGroup(
    id = id,
    recipeType = recipeType,
    currentRewardGrade = currentRewardGrade,
    rerollCostEcoJam = rerollCostEcoJam,
    active = active,
)

fun createSoupRerollPolicyCandidate(
    id: Long = 1L,
    soupRerollPolicyGroup: SoupRerollPolicyGroup = createSoupRerollPolicyGroup(),
    nextRewardGrade: SoupRewardGrade = SoupRewardGrade.SMALL,
    probability: BigDecimal = BigDecimal("100.00"),
    pointAmount: Int = 0,
    ecoJamAmount: Int = 0,
    active: Boolean = true,
): SoupRerollPolicyCandidate = SoupRerollPolicyCandidate(
    id = id,
    soupRerollPolicyGroup = soupRerollPolicyGroup,
    nextRewardGrade = nextRewardGrade,
    probability = probability,
    pointAmount = pointAmount,
    ecoJamAmount = ecoJamAmount,
    active = active,
)

fun createSoupRerollPolicyIngredient(
    id: Long = 1L,
    soupRerollPolicyCandidate: SoupRerollPolicyCandidate = createSoupRerollPolicyCandidate(),
    selectionType: SoupRewardIngredientSelectionType = SoupRewardIngredientSelectionType.RANDOM_BY_TYPE,
    ingredient: Ingredient? = null,
    ingredientType: IngredientType? = IngredientType.COMMON,
    quantity: Int = 1,
): SoupRerollPolicyIngredient = SoupRerollPolicyIngredient(
    id = id,
    soupRerollPolicyCandidate = soupRerollPolicyCandidate,
    selectionType = selectionType,
    ingredient = ingredient,
    ingredientType = ingredientType,
    quantity = quantity,
)

fun createSoupRewardIngredient(
    id: Long = 1L,
    soup: Soup = createSoup(),
    ingredient: Ingredient = createIngredient(),
    quantity: Int = 1,
): SoupRewardIngredient = SoupRewardIngredient(
    id = id,
    soup = soup,
    ingredient = ingredient,
    quantity = quantity,
)

fun createSubmitMissionVerificationRequestBody(
    photoKey: String = "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
): String = """
    {"photoKey":"$photoKey"}
""".trimIndent()

fun createEcoJamHistory(
    id: Long = 1L,
    user: User = createUser(),
    amount: Int = 300,
    sourceType: EcoJamHistorySourceType = EcoJamHistorySourceType.SOUP,
    sourceId: Long = 10L,
    createdAt: LocalDateTime = LocalDateTime.of(2026, 7, 19, 22, 30, 0),
): EcoJamHistory = EcoJamHistory(
    id = id,
    user = user,
    amount = amount,
    sourceType = sourceType,
    sourceId = sourceId,
).apply {
    this.createdAt = createdAt
    this.updatedAt = createdAt
}

fun createPointHistory(
    id: Long = 1L,
    user: User = createUser(),
    amount: Int = 500,
    sourceType: PointHistorySourceType = PointHistorySourceType.SOUP,
    sourceId: Long = 10L,
    createdAt: LocalDateTime = LocalDateTime.of(2026, 7, 19, 22, 30, 0),
): PointHistory = PointHistory(
    id = id,
    user = user,
    amount = amount,
    sourceType = sourceType,
    sourceId = sourceId,
).apply {
    this.createdAt = createdAt
    this.updatedAt = createdAt
}
