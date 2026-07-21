package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.support.createUser
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
class AdminMissionReviewConcurrencyTest(
    @Autowired private val adminMissionReviewService: AdminMissionReviewService,
    @Autowired private val missionCompletionRepository: MissionCompletionRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val missionRepository: com.zerost.api.mission.domain.MissionRepository,
    @Autowired private val ingredientRepository: IngredientRepository,
    @Autowired private val userIngredientRepository: UserIngredientRepository,
) {

    @Test
    fun `같은 미션 인증을 동시에 승인해도 한 번만 보상이 지급된다`() {
        val user = userRepository.save(createUser(id = null))
        val ingredient = ingredientRepository.save(
            Ingredient(
                name = "버려진 천",
                type = IngredientType.COMMON,
                imageUrl = null,
            ),
        )
        val mission = missionRepository.save(
            Mission(
                title = "텀블러 사용하기",
                description = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.",
                imageUrl = null,
                rewardIngredientPool = "[${requireNotNull(ingredient.id)}]",
            ),
        )
        val completion = missionCompletionRepository.save(
            MissionCompletion.submit(
                user = user,
                mission = mission,
                photoKey = "missions/${requireNotNull(user.id)}/${requireNotNull(mission.id)}/2026/07/19/review.jpg",
                submittedAt = LocalDateTime.of(2026, 7, 19, 12, 0, 0),
            ),
        )

        val executor = Executors.newFixedThreadPool(2)
        val startLatch = CountDownLatch(1)

        val results = try {
            val futures = listOf(
                executor.submit(submitTask(startLatch, requireNotNull(completion.id))),
                executor.submit(submitTask(startLatch, requireNotNull(completion.id))),
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
        assertEquals(ErrorCode.INVALID_MISSION_REVIEW_STATUS, exception.errorCode)

        val reviewedCompletion = missionCompletionRepository.findById(requireNotNull(completion.id)).orElseThrow()
        assertEquals(MissionCompletionStatus.APPROVED, reviewedCompletion.status)
        assertEquals(requireNotNull(ingredient.id), reviewedCompletion.rewardedIngredient?.id)

        val userIngredient = userIngredientRepository.findByUserAndIngredient(user, ingredient).orElseThrow()
        assertEquals(1, userIngredient.quantity)
    }

    private fun submitTask(
        startLatch: CountDownLatch,
        completionId: Long,
    ): Callable<Result<Unit>> = Callable {
        startLatch.await()
        runCatching {
            adminMissionReviewService.reviewMissionCompletion(
                completionId = completionId,
                status = "APPROVED",
            )
        }.map { Unit }
    }
}
