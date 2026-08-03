package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class NicknameCommandServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val nicknameCommandService = NicknameCommandService(userRepository)

    @Test
    fun `닉네임을 변경할 수 있다`() {
        val user = createUser(id = 1L, nickname = "기존닉네임")
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.findByNickname("새닉네임")).thenReturn(Optional.empty())

        val response = nicknameCommandService.updateNickname(1L, "새닉네임")

        assertEquals(1L, response.userId)
        assertEquals("새닉네임", response.nickname)
        assertEquals("새닉네임", user.nickname)
    }

    @Test
    fun `같은 닉네임으로 변경 요청하면 그대로 성공한다`() {
        val user = createUser(id = 1L, nickname = "기존닉네임")
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.findByNickname("기존닉네임")).thenReturn(Optional.of(user))

        val response = nicknameCommandService.updateNickname(1L, "기존닉네임")

        assertEquals(1L, response.userId)
        assertEquals("기존닉네임", response.nickname)
        assertEquals("기존닉네임", user.nickname)
    }

    @Test
    fun `이미 사용 중인 닉네임으로는 변경할 수 없다`() {
        val user = createUser(id = 1L, nickname = "기존닉네임")
        val duplicatedUser = createUser(id = 2L, nickname = "새닉네임")
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.findByNickname("새닉네임")).thenReturn(Optional.of(duplicatedUser))

        val exception = assertThrows<BusinessException> {
            nicknameCommandService.updateNickname(1L, "새닉네임")
        }

        assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.errorCode)
    }
}
