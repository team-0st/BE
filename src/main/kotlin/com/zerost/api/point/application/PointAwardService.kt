package com.zerost.api.point.application

import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.user.domain.User
import org.springframework.stereotype.Service

@Service
class PointAwardService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointPolicyProperties: PointPolicyProperties,
) {

    fun award(
        user: User,
        requestedAmount: Int,
        sourceType: PointHistorySourceType,
        sourceId: Long,
    ): Int {
        require(requestedAmount >= 0) { "지급 요청 포인트는 0 이상이어야 합니다." }

        if (requestedAmount == 0) {
            return 0
        }

        val grantedAmount = resolveGrantedAmount(requireNotNull(user.id), requestedAmount)
        if (grantedAmount == 0) {
            return 0
        }

        user.increasePoint(grantedAmount)
        pointHistoryRepository.save(
            PointHistory.earn(
                user = user,
                amount = grantedAmount,
                sourceType = sourceType,
                sourceId = sourceId,
            ),
        )

        return grantedAmount
    }

    private fun resolveGrantedAmount(
        userId: Long,
        requestedAmount: Int,
    ): Int {
        val maxCumulativeEarnAmountPerUser = pointPolicyProperties.maxCumulativeEarnAmountPerUser
            ?: return requestedAmount

        val currentEarnedAmount = pointHistoryRepository.sumEarnedAmountByUserId(userId)
        val remainingAmount = (maxCumulativeEarnAmountPerUser - currentEarnedAmount).coerceAtLeast(0)

        return requestedAmount.coerceAtMost(remainingAmount)
    }
}
