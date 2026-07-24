package com.zerost.api.point.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class PointAwardService(
    private val userRepository: UserRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointPolicyProperties: PointPolicyProperties,
) {
    private val maxCumulativeEarnAmountPerUser: Int? = pointPolicyProperties.resolvedMaxCumulativeEarnAmountPerUser()

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

        val lockedUser = userRepository.findByIdForUpdate(requireNotNull(user.id))
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val grantedAmount = resolveGrantedAmount(requireNotNull(lockedUser.id), requestedAmount)
        if (grantedAmount == 0) {
            return 0
        }

        lockedUser.increasePoint(grantedAmount)
        pointHistoryRepository.save(
            PointHistory.earn(
                user = lockedUser,
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
        val maxCumulativeEarnAmountPerUser = maxCumulativeEarnAmountPerUser
            ?: return requestedAmount

        val currentEarnedAmount = pointHistoryRepository.sumEarnedAmountByUserId(userId)
        val remainingAmount = (maxCumulativeEarnAmountPerUser.toLong() - currentEarnedAmount).coerceAtLeast(0L)

        return requestedAmount.toLong()
            .coerceAtMost(remainingAmount)
            .toInt()
    }
}
