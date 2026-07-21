package com.zerost.api.profile.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class ProfileCharacterCommandServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val profileCharacterCommandService = ProfileCharacterCommandService(userRepository)

    @Test
    fun `프로필 캐릭터를 선택할 수 있다`() {
        val user = createUser()
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))

        val response = profileCharacterCommandService.updateProfileCharacter(1L, "BASIC_3")

        assertEquals(1L, response.userId)
        assertEquals("BASIC_3", response.profileCharacterCode)
        assertEquals(ProfileCharacterCode.BASIC_3, user.profileCharacterCode)
    }

    @Test
    fun `유효하지 않은 프로필 캐릭터 코드는 선택할 수 없다`() {
        val user = createUser()
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))

        val exception = assertThrows<BusinessException> {
            profileCharacterCommandService.updateProfileCharacter(1L, "UNKNOWN")
        }

        assertEquals(ErrorCode.INVALID_PROFILE_CHARACTER_CODE, exception.errorCode)
    }
}
