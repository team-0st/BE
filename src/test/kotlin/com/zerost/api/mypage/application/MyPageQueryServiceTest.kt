package com.zerost.api.mypage.application

import com.zerost.api.common.config.PublicAssetsProperties
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.profile.application.ProfileCharacterImageUrlResolver
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createShop
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class MyPageQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val soupRepository = mock(SoupRepository::class.java)
    private val profileCharacterImageUrlResolver = ProfileCharacterImageUrlResolver(
        PublicAssetsProperties(
            baseUrl = "https://assets.zero-st.com",
        ),
    )
    private val myPageQueryService = MyPageQueryService(
        userRepository = userRepository,
        userIngredientRepository = userIngredientRepository,
        missionCompletionRepository = missionCompletionRepository,
        soupRepository = soupRepository,
        profileCharacterImageUrlResolver = profileCharacterImageUrlResolver,
    )

    @Test
    fun `마이페이지 조회 시 프로필과 보유 재료와 누적 통계를 함께 반환한다`() {
        val shop = createShop(name = "알맹상점")
        val user = createUser(
            nickname = "펭귄탐험가",
            profileCharacterCode = ProfileCharacterCode.CABBAGE,
            shop = shop,
            ecoJam = 410,
            point = 2200,
            onboardingCompleted = true,
        )
        val cabbage = createIngredient(id = 1L, name = "양배추", imageUrl = "image-1")
        val tomato = createIngredient(id = 2L, name = "토마토", imageUrl = "image-2")

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userIngredientRepository.findAllByUserIdOrderByIdAsc(1L)).thenReturn(
            listOf(
                createUserIngredient(id = 1L, user = user, ingredient = cabbage, quantity = 2),
                createUserIngredient(id = 2L, user = user, ingredient = tomato, quantity = 5),
            ),
        )
        `when`(soupRepository.countByUserId(1L)).thenReturn(4L)
        `when`(missionCompletionRepository.countByUserIdAndStatus(1L, MissionCompletionStatus.APPROVED)).thenReturn(3L)

        val response = myPageQueryService.getMyPage(1L)

        assertEquals("펭귄탐험가", response.nickname)
        assertEquals("CABBAGE", response.profileCharacterCode)
        assertEquals("https://assets.zero-st.com/profile-characters/cabbage.png", response.profileCharacterImageUrl)
        assertEquals("알맹상점", response.shopName)
        assertEquals(410, response.ecoJam)
        assertEquals(2200, response.point)
        assertEquals(4, response.brewedSoupCount)
        assertEquals(3, response.completedMissionCount)
        assertEquals(7, response.totalIngredientQuantity)
        assertEquals(2, response.ingredients.size)
        assertEquals("양배추", response.ingredients[0].name)
        assertEquals(5, response.ingredients[1].quantity)
    }
}
