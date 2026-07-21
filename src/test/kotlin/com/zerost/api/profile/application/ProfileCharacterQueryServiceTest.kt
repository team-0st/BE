package com.zerost.api.profile.application

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileCharacterQueryServiceTest {

    private val profileCharacterQueryService = ProfileCharacterQueryService()

    @Test
    fun `선택 가능한 프로필 캐릭터 목록을 조회할 수 있다`() {
        val response = profileCharacterQueryService.getProfileCharacters()

        assertEquals(4, response.size)
        assertEquals("BASIC_1", response[0].code)
        assertTrue(response[0].name.isNotBlank())
        assertTrue(response[0].description.isNotBlank())
    }
}
