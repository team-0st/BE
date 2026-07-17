package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class MissionVerificationConcurrencyTest(
    @Autowired private val missionVerificationService: MissionVerificationService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val missionRepository: MissionRepository,
    @Autowired private val missionCompletionRepository: MissionCompletionRepository,
) {

    @Test
    fun `같은 유저가 같은 미션을 동시에 제출해도 하나만 성공한다`() {
        val deviceId = "concurrency-device-${UUID.randomUUID()}"
        val user = userRepository.save(
            User(deviceId = deviceId),
        )
        val mission = missionRepository.save(
            Mission(
                title = "텀블러 사용하기",
                description = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.",
                imageUrl = null,
                rewardIngredientPool = "[1,2,3]",
            ),
        )

        val executor = Executors.newFixedThreadPool(2)
        val startLatch = CountDownLatch(1)

        val futures = listOf(
            executor.submit(submitTask(startLatch, deviceId, requireNotNull(mission.id), "https://example.com/1.jpg")),
            executor.submit(submitTask(startLatch, deviceId, requireNotNull(mission.id), "https://example.com/2.jpg")),
        )

        startLatch.countDown()

        val results = futures.map { it.get(5, TimeUnit.SECONDS) }
        executor.shutdown()

        val successCount = results.count { it.isSuccess }
        val failureResults = results.filter { it.isFailure }

        assertEquals(1, successCount)
        assertEquals(1, failureResults.size)

        val exception = failureResults.single().exceptionOrNull()
        assertTrue(exception is BusinessException)
        assertEquals(ErrorCode.MISSION_UNDER_REVIEW, exception.errorCode)

        val completions = missionCompletionRepository.findAllByUserIdOrderBySubmittedAtDesc(requireNotNull(user.id))
        assertEquals(1, completions.size)
    }

    private fun submitTask(
        startLatch: CountDownLatch,
        deviceId: String,
        missionId: Long,
        photoUrl: String,
    ): Callable<Result<Unit>> = Callable {
        startLatch.await()
        runCatching {
            missionVerificationService.submitVerification(
                deviceId = deviceId,
                missionId = missionId,
                photoUrl = photoUrl,
            )
        }.map { Unit }
    }
}
