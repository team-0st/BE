package com.zerost.api.common.auth

import com.zerost.api.auth.application.AccessTokenClaims
import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthenticationInterceptorTest {

    private val authTokenProvider = mock(AuthTokenProvider::class.java)
    private val interceptor = AuthenticationInterceptor(authTokenProvider)

    @Test
    fun `Authorization 헤더가 있으면 access token에서 사용자 식별 정보를 꺼낸다`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer access-token")
        }
        val response = mock(HttpServletResponse::class.java)

        `when`(authTokenProvider.parseAccessToken("access-token")).thenReturn(
            AccessTokenClaims(userId = 1L),
        )

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
        assertEquals(1L, request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE))
        verify(authTokenProvider).parseAccessToken("access-token")
    }

    @Test
    fun `Authorization 헤더가 없으면 예외가 발생한다`() {
        val request = MockHttpServletRequest()
        val response = mock(HttpServletResponse::class.java)

        val exception = assertFailsWith<BusinessException> {
            interceptor.preHandle(request, response, Any())
        }

        assertEquals(ErrorCode.ACCESS_TOKEN_REQUIRED, exception.errorCode)
    }
}
