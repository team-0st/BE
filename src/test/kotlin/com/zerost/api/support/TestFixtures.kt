package com.zerost.api.support

import com.zerost.api.shop.domain.Shop
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.communitymission.domain.CommunityMission
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.mission.domain.Mission
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
import com.zerost.api.user.application.CompleteOnboardingCommand
import com.zerost.api.user.domain.User
import java.math.BigDecimal
import java.time.LocalDateTime

fun createUser(
    id: Long = 1L,
    deviceId: String = "device-1",
    nickname: String? = null,
    phoneNumber: String? = null,
    passwordHash: String? = null,
    shop: Shop? = null,
    ecoJam: Int = 0,
    point: Int = 0,
    onboardingCompleted: Boolean = false,
): User = User(
    id = id,
    deviceId = deviceId,
    nickname = nickname,
    phoneNumber = phoneNumber,
    passwordHash = passwordHash,
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
    deviceId: String = "device-1",
    nickname: String = "펭귄탐험가",
    phoneNumber: String = "010-1234-5678",
    password: String = "zerost1234",
    shopId: Long = 1L,
): CompleteOnboardingCommand = CompleteOnboardingCommand(
    deviceId = deviceId,
    nickname = nickname,
    phoneNumber = phoneNumber,
    password = password,
    shopId = shopId,
)

fun createRegisterUserRequestBody(
    deviceId: String = "device-1",
): String = """
    {"deviceId":"$deviceId"}
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

fun createMission(
    id: Long = 1L,
    title: String = "텀블러 사용하기",
    description: String? = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.",
    imageUrl: String? = "https://example.com/images/mission-1.png",
    rewardIngredientPool: String = "[1,2,3]",
): Mission = Mission(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    rewardIngredientPool = rewardIngredientPool,
)

fun createMissionCompletion(
    id: Long = 1L,
    user: User = createUser(),
    mission: Mission = createMission(),
    photoKey: String = "missions/device-1/1/2026/07/18/mission-1.jpg",
    status: MissionCompletionStatus = MissionCompletionStatus.PENDING,
    rewardedIngredient: Ingredient? = null,
    submittedAt: LocalDateTime = LocalDateTime.of(2026, 7, 17, 10, 0, 0),
    reviewedAt: LocalDateTime? = null,
): MissionCompletion = MissionCompletion(
    id = id,
    user = user,
    mission = mission,
    photoKey = photoKey,
    status = status,
    rewardedIngredient = rewardedIngredient,
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
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
): CommunityMission = CommunityMission(
    id = id,
    title = title,
    description = description,
    difficulty = difficulty,
    stage = stage,
    targetRatio = targetRatio,
    imageUrl = imageUrl,
    active = active,
)

fun createCommunityMissionCompletion(
    id: Long = 1L,
    communityMission: CommunityMission = createCommunityMission(),
    user: User = createUser(),
    completedAt: LocalDateTime = LocalDateTime.of(2026, 7, 21, 10, 0, 0),
): CommunityMissionCompletion = CommunityMissionCompletion(
    id = id,
    communityMission = communityMission,
    user = user,
    completedAt = completedAt,
)

fun createRecipe(
    id: Long = 1L,
    name: String = "오리지널 스프",
    type: RecipeType = RecipeType.COMMON,
    slotCount: Int = 3,
    hidden: Boolean = false,
): Recipe = Recipe(
    id = id,
    name = name,
    type = type,
    slotCount = slotCount,
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
    photoKey: String = "missions/device-1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
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
