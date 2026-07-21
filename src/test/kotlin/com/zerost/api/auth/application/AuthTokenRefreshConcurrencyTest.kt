package com.zerost.api.auth.application

import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class AuthTokenRefreshConcurrencyTest(
    @Autowired private val authTokenService: AuthTokenService,
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
    @Autowired private val refreshTokenHasher: RefreshTokenHasher,
    @Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `같은 refresh token으로 동시에 재발급 요청해도 하나만 성공한다`() {
        val user = userRepository.save(createUser(id = null))
        val rawRefreshToken = "concurrent-refresh-token"
        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                tokenHash = refreshTokenHasher.hash(rawRefreshToken),
                expiresAt = LocalDateTime.now().plusDays(7),
            ),
        )

        val executor = Executors.newFixedThreadPool(2)
        val startLatch = CountDownLatch(1)

        val results = try {
            val futures = listOf(
                executor.submit(submitTask(startLatch, rawRefreshToken)),
                executor.submit(submitTask(startLatch, rawRefreshToken)),
            )

            startLatch.countDown()
            futures.map { it.get(5, TimeUnit.SECONDS) }
        } finally {
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }

        val successCount = results.count { it.isSuccess }
        val failureResults = results.filter { it.isFailure }

        assertEquals(1, successCount)
        assertEquals(1, failureResults.size)

        val exception = failureResults.single().exceptionOrNull()
        assertTrue(exception is BusinessException)
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
    }

    private fun submitTask(
        startLatch: CountDownLatch,
        refreshToken: String,
    ): Callable<Result<Unit>> = Callable {
        startLatch.await()
        runCatching {
            authTokenService.refresh(refreshToken)
        }.map { Unit }
    }
}
