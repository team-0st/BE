package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.support.createUser
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`

@SpringBootTest
@ActiveProfiles("test")
class MissionVerificationConcurrencyTest(
    @Autowired private val missionVerificationService: MissionVerificationService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val missionRepository: MissionRepository,
    @Autowired private val missionCompletionRepository: MissionCompletionRepository,
) {

    @MockitoBean
    lateinit var fileUploadService: FileUploadService

    @Test
    fun `같은 유저가 같은 미션을 동시에 제출해도 하나만 성공한다`() {
        val user = userRepository.save(
            createUser(id = null),
        )
        val mission = missionRepository.save(
            Mission(
                title = "텀블러 사용하기",
                description = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.",
                imageUrl = null,
                rewardIngredientPool = "[1,2,3]",
            ),
        )
        val userId = requireNotNull(user.id)
        val photoKey = "missions/$userId/${requireNotNull(mission.id)}/2026/07/18/mission.jpg"
        doNothing().`when`(fileUploadService).validateMissionImageKey(userId, requireNotNull(mission.id), photoKey)

        val executor = Executors.newFixedThreadPool(2)
        val startLatch = CountDownLatch(1)

        val results = try {
            val futures = listOf(
                executor.submit(submitTask(startLatch, userId, requireNotNull(mission.id), photoKey)),
                executor.submit(submitTask(startLatch, userId, requireNotNull(mission.id), photoKey)),
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
        assertEquals(ErrorCode.MISSION_UNDER_REVIEW, exception.errorCode)

        val completions = missionCompletionRepository.findAllByUserIdOrderBySubmittedAtDesc(requireNotNull(user.id))
        assertEquals(1, completions.size)
    }

    private fun submitTask(
        startLatch: CountDownLatch,
        userId: Long,
        missionId: Long,
        photoKey: String,
    ): Callable<Result<Unit>> = Callable {
        startLatch.await()
        runCatching {
            missionVerificationService.submitVerification(
                userId = userId,
                missionId = missionId,
                photoKey = photoKey,
            )
        }.map { Unit }
    }
}
