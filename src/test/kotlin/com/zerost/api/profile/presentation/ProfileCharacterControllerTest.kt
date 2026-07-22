package com.zerost.api.profile.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.profile.application.ProfileCharacterCommandService
import com.zerost.api.profile.application.ProfileCharacterQueryService
import com.zerost.api.profile.presentation.dto.ProfileCharacterResponse
import com.zerost.api.profile.presentation.dto.UpdateProfileCharacterResponse
import com.zerost.api.support.createAuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class ProfileCharacterControllerTest {

    private val profileCharacterQueryService = mock(ProfileCharacterQueryService::class.java)
    private val profileCharacterCommandService = mock(ProfileCharacterCommandService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(
            ProfileCharacterController(profileCharacterQueryService, profileCharacterCommandService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `선택 가능한 프로필 캐릭터 목록을 조회할 수 있다`() {
        `when`(profileCharacterQueryService.getProfileCharacters()).thenReturn(
            listOf(
                ProfileCharacterResponse(
                    code = "BROCCOLI",
                    name = "브로콜리",
                    description = "브로콜리 프로필 캐릭터",
                    imageUrl = "https://assets.zero-st.com/profile-characters/broccoli.png",
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/profile-characters")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].code").value("BROCCOLI"))
            .andExpect(jsonPath("$.data[0].name").value("브로콜리"))
            .andExpect(jsonPath("$.data[0].imageUrl").value("https://assets.zero-st.com/profile-characters/broccoli.png"))

        verify(profileCharacterQueryService).getProfileCharacters()
    }

    @Test
    fun `프로필 캐릭터를 선택할 수 있다`() {
        `when`(profileCharacterCommandService.updateProfileCharacter(1L, "CARROT")).thenReturn(
            UpdateProfileCharacterResponse(
                userId = 1L,
                profileCharacterCode = "CARROT",
                profileCharacterImageUrl = "https://assets.zero-st.com/profile-characters/carrot.png",
            ),
        )

        mockMvc.perform(
            patch("/api/v1/profile-characters/me")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"profileCharacterCode":"CARROT"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.profileCharacterCode").value("CARROT"))
            .andExpect(jsonPath("$.data.profileCharacterImageUrl").value("https://assets.zero-st.com/profile-characters/carrot.png"))

        verify(profileCharacterCommandService).updateProfileCharacter(1L, "CARROT")
    }
}
