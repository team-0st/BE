package com.zerost.api.testerlink.presentation

import com.zerost.api.common.auth.AdminAuthorizationInterceptor
import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.testerlink.application.TesterLinkService
import com.zerost.api.testerlink.presentation.dto.AdminTesterLinkResponse
import com.zerost.api.testerlink.presentation.dto.CurrentTesterLinkResponse
import com.zerost.api.user.domain.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class AdminTesterLinkControllerTest {

    private val testerLinkService = mock(TesterLinkService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminTesterLinkController(testerLinkService),
            TesterLinkController(testerLinkService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(
                AuthenticationInterceptor(createAuthTokenProvider(role = UserRole.ADMIN)),
                AdminAuthorizationInterceptor(),
            )
            .build()
    }

    @Test
    fun `관리자는 테스트 링크를 조회할 수 있다`() {
        `when`(testerLinkService.getAdminTesterLink()).thenReturn(
            AdminTesterLinkResponse(
                shareUrl = "https://zero-st.com/open",
                deepLink = "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
                deploymentId = "019f893b-a962-71de-b3ea-2c2544ad7afa",
                tossShareUrl = "https://toss.im/_m/example",
                updatedAt = "2026-07-23T01:30:00",
            ),
        )

        mockMvc.perform(
            get("/api/v1/admin/tester-link").header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.shareUrl").value("https://zero-st.com/open"))
            .andExpect(jsonPath("$.data.tossShareUrl").value("https://toss.im/_m/example"))

        verify(testerLinkService).getAdminTesterLink()
    }

    @Test
    fun `관리자는 테스트 링크를 갱신할 수 있다`() {
        `when`(
            testerLinkService.updateTesterLink(
                "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
                "https://toss.im/_m/example",
                1L,
            ),
        ).thenReturn(
            AdminTesterLinkResponse(
                shareUrl = "https://zero-st.com/open",
                deepLink = "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
                deploymentId = "019f893b-a962-71de-b3ea-2c2544ad7afa",
                tossShareUrl = "https://toss.im/_m/example",
                updatedAt = "2026-07-23T01:30:00",
            ),
        )

        mockMvc.perform(
            put("/api/v1/admin/tester-link")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "deepLink":"intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
                      "tossShareUrl":"https://toss.im/_m/example"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.deploymentId").value("019f893b-a962-71de-b3ea-2c2544ad7afa"))
            .andExpect(jsonPath("$.data.tossShareUrl").value("https://toss.im/_m/example"))
    }

    @Test
    fun `일반 유저는 관리자 테스트 링크를 조회할 수 없다`() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminTesterLinkController(testerLinkService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(
                AuthenticationInterceptor(createAuthTokenProvider(role = UserRole.USER)),
                AdminAuthorizationInterceptor(),
            )
            .build()

        mockMvc.perform(
            get("/api/v1/admin/tester-link").header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `공개 current는 서비스 응답을 그대로 반환한다`() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            TesterLinkController(testerLinkService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

        `when`(testerLinkService.getCurrentTesterLink()).thenReturn(
            CurrentTesterLinkResponse(
                deepLink = "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
                deploymentId = "019f893b-a962-71de-b3ea-2c2544ad7afa",
                tossShareUrl = "https://toss.im/_m/example",
            ),
        )

        mockMvc.perform(get("/api/v1/tester-link/current"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.deploymentId").value("019f893b-a962-71de-b3ea-2c2544ad7afa"))
            .andExpect(jsonPath("$.data.tossShareUrl").value("https://toss.im/_m/example"))
    }
}
