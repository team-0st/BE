package com.zerost.api.support

import com.zerost.api.shop.domain.Shop
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.user.application.CompleteOnboardingCommand
import com.zerost.api.user.domain.User

fun createUser(
    id: Long = 1L,
    deviceId: String = "device-1",
    nickname: String? = null,
    phoneNumber: String? = null,
    shop: Shop? = null,
    onboardingCompleted: Boolean = false,
): User = User(
    id = id,
    deviceId = deviceId,
    nickname = nickname,
    phoneNumber = phoneNumber,
    shop = shop,
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
    shopId: Long = 1L,
): CompleteOnboardingCommand = CompleteOnboardingCommand(
    deviceId = deviceId,
    nickname = nickname,
    phoneNumber = phoneNumber,
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
          "phoneNumber": "$phoneNumber"$shopIdField
        }
    """.trimIndent()
}
