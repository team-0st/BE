package com.zerost.api.common.auth

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.UserRole
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AdminAuthorizationInterceptorTest {

    private val interceptor = AdminAuthorizationInterceptor()

    @Test
    fun `관리자 권한이면 관리자 API 접근을 허용한다`() {
        val request = MockHttpServletRequest().apply {
            setAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE, UserRole.ADMIN)
        }
        val response = mock(HttpServletResponse::class.java)

        val result = interceptor.preHandle(request, response, Any())

        assertTrue(result)
    }

    @Test
    fun `일반 유저 권한이면 관리자 API 접근을 차단한다`() {
        val request = MockHttpServletRequest().apply {
            setAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE, UserRole.USER)
        }
        val response = mock(HttpServletResponse::class.java)

        val exception = assertFailsWith<BusinessException> {
            interceptor.preHandle(request, response, Any())
        }

        assertEquals(ErrorCode.ADMIN_ACCESS_DENIED, exception.errorCode)
    }
}
