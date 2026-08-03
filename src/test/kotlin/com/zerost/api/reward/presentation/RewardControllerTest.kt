package com.zerost.api.reward.presentation

import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.reward.application.RewardClaimService
import com.zerost.api.reward.application.RewardQueryService
import com.zerost.api.reward.presentation.dto.ClaimAllRewardsResponse
import com.zerost.api.reward.presentation.dto.ClaimRewardResponse
import com.zerost.api.reward.presentation.dto.RewardBundleResponse
import com.zerost.api.reward.presentation.dto.RewardEntryResponse
import com.zerost.api.reward.presentation.dto.RewardsSummaryResponse
import com.zerost.api.reward.presentation.dto.RewardsTabResponse
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.user.domain.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class RewardControllerTest {

    private val rewardQueryService = mock(RewardQueryService::class.java)
    private val rewardClaimService = mock(RewardClaimService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            RewardController(rewardQueryService, rewardClaimService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(AuthenticationInterceptor(createAuthTokenProvider(role = UserRole.USER)))
            .build()
    }

    @Test
    fun `보상 탭을 조회할 수 있다`() {
        `when`(rewardQueryService.getRewards(1L)).thenReturn(
            RewardsTabResponse(
                summary = RewardsSummaryResponse(
                    totalPendingRewardCount = 1,
                    pendingEcoJam = 0,
                    pendingPoint = 0,
                    pendingCommonIngredientCount = 1,
                    pendingHiddenIngredientCount = 0,
                ),
                items = listOf(
                    RewardBundleResponse(
                        rewardId = 55L,
                        rewardSourceType = "MISSION",
                        sourceId = 55L,
                        sourceTitle = "텀블러 사용 인증",
                        rewardStatus = "CLAIMABLE",
                        earnedAt = "2026-07-22T18:00:00",
                        claimedAt = null,
                        rewards = listOf(
                            RewardEntryResponse(
                                rewardType = "INGREDIENT",
                                ingredientType = "COMMON",
                                ingredientId = 2L,
                                ingredientName = "토마토",
                                quantity = 1,
                                imageUrl = "https://assets.zero-st.com/ingredients/tomato.png",
                            ),
                        ),
                    ),
                ),
            ),
        )

        mockMvc.perform(get("/api/v1/rewards").header("Authorization", "Bearer access-token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.summary.totalPendingRewardCount").value(1))
            .andExpect(jsonPath("$.data.items[0].rewardId").value(55))
            .andExpect(jsonPath("$.data.items[0].rewardSourceType").value("MISSION"))

        verify(rewardQueryService).getRewards(1L)
    }

    @Test
    fun `보상을 개별 수령할 수 있다`() {
        `when`(rewardClaimService.claimReward(1L, 55L)).thenReturn(
            ClaimRewardResponse(rewardId = 55L, claimedAt = "2026-07-23T10:30:00"),
        )

        mockMvc.perform(post("/api/v1/rewards/55/claim").header("Authorization", "Bearer access-token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.rewardId").value(55))
            .andExpect(jsonPath("$.data.claimedAt").value("2026-07-23T10:30:00"))

        verify(rewardClaimService).claimReward(1L, 55L)
    }

    @Test
    fun `보상을 일괄 수령할 수 있다`() {
        `when`(rewardClaimService.claimAll(1L)).thenReturn(
            ClaimAllRewardsResponse(claimedRewardCount = 2, claimedAt = "2026-07-23T10:31:00"),
        )

        mockMvc.perform(post("/api/v1/rewards/claim-all").header("Authorization", "Bearer access-token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.claimedRewardCount").value(2))

        verify(rewardClaimService).claimAll(1L)
    }
}
