package com.zerost.api.user.presentation

import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.support.createRegisterUserRequestBody
import com.zerost.api.support.createUpdateNicknameRequestBody
import com.zerost.api.user.application.NicknameCommandService
import com.zerost.api.user.application.UserRegistrationService
import com.zerost.api.user.presentation.dto.UpdateNicknameResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class UserControllerTest {

    private val userRegistrationService = mock(UserRegistrationService::class.java)
    private val nicknameCommandService = mock(NicknameCommandService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(UserController(userRegistrationService, nicknameCommandService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(AuthenticationInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `임시 유저 등록에 성공하면 유저 정보와 토큰을 반환한다`() {
        `when`(userRegistrationService.register())
            .thenReturn(
                RegisterUserResponse(
                    userId = 1L,
                    onboardingCompleted = false,
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    tokenType = "Bearer",
                    accessTokenExpiresIn = 3600,
                    refreshTokenExpiresIn = 1209600,
                ),
            )

        mockMvc.perform(
                post("/api/v1/users/register")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRegisterUserRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.onboardingCompleted").value(false))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))

        verify(userRegistrationService).register()
    }

    @Test
    fun `닉네임을 변경할 수 있다`() {
        `when`(nicknameCommandService.updateNickname(1L, "새닉네임"))
            .thenReturn(
                UpdateNicknameResponse(
                    userId = 1L,
                    nickname = "새닉네임",
                ),
            )

        mockMvc.perform(
            patch("/api/v1/users/me/nickname")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateNicknameRequestBody(nickname = "새닉네임")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.nickname").value("새닉네임"))

        verify(nicknameCommandService).updateNickname(1L, "새닉네임")
    }

    @Test
    fun `닉네임이 비어 있으면 변경에 실패한다`() {
        mockMvc.perform(
            patch("/api/v1/users/me/nickname")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUpdateNicknameRequestBody(nickname = "")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }
}
