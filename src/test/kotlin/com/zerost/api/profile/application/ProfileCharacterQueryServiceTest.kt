package com.zerost.api.profile.application

import com.zerost.api.common.config.PublicAssetsProperties
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileCharacterQueryServiceTest {

    private val profileCharacterQueryService = ProfileCharacterQueryService(
        ProfileCharacterImageUrlResolver(
            PublicAssetsProperties(
                baseUrl = "https://assets.zero-st.com",
            ),
        ),
    )

    @Test
    fun `선택 가능한 프로필 캐릭터 목록을 조회할 수 있다`() {
        val response = profileCharacterQueryService.getProfileCharacters()

        assertEquals(10, response.size)
        assertEquals("BROCCOLI", response[0].code)
        assertTrue(response[0].name.isNotBlank())
        assertTrue(response[0].description.isNotBlank())
        assertEquals("https://assets.zero-st.com/profile-characters/broccoli.png", response[0].imageUrl)
    }
}
