package com.zerost.api.admin.asset.application

import com.zerost.api.admin.asset.presentation.dto.AdminAssetGrantItemResult
import com.zerost.api.admin.asset.presentation.dto.AdminAssetGrantRequest
import com.zerost.api.admin.asset.presentation.dto.AdminAssetGrantResponse
import com.zerost.api.admin.asset.presentation.dto.AdminAssetType
import com.zerost.api.admin.asset.presentation.dto.AdminUserSummaryResponse
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAssetGrantService(
    private val userRepository: UserRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) {

    @Transactional(readOnly = true)
    fun listUsers(): List<AdminUserSummaryResponse> {
        return userRepository.findAll()
            .sortedBy { it.id }
            .map { user ->
                AdminUserSummaryResponse(
                    userId = requireNotNull(user.id),
                    nickname = user.nickname,
                    phoneNumber = user.phoneNumber,
                    onboardingCompleted = user.onboardingCompleted,
                    ecoJam = user.ecoJam,
                    point = user.point,
                )
            }
    }

    @Transactional
    fun grant(adminUserId: Long, request: AdminAssetGrantRequest): AdminAssetGrantResponse {
        if (request.amount <= 0) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        if (request.userIds.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val grantByUserId = request.userIds
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, count) -> request.amount * count }

        val results = mutableListOf<AdminAssetGrantItemResult>()
        var totalGrantedAmount = 0

        for ((userId, grantedAmount) in grantByUserId) {
            val user = userRepository.findByIdForUpdate(userId)
                .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

            when (request.assetType) {
                AdminAssetType.ECO_JAM -> {
                    user.increaseEcoJam(grantedAmount)
                    ecoJamHistoryRepository.save(
                        EcoJamHistory.earn(
                            user = user,
                            amount = grantedAmount,
                            sourceType = EcoJamHistorySourceType.ADMIN_GRANT,
                            sourceId = adminUserId,
                        ),
                    )
                    results += AdminAssetGrantItemResult(
                        userId = userId,
                        grantedAmount = grantedAmount,
                        balanceAfter = user.ecoJam,
                    )
                }
                AdminAssetType.POINT -> {
                    user.increasePoint(grantedAmount)
                    pointHistoryRepository.save(
                        PointHistory.earn(
                            user = user,
                            amount = grantedAmount,
                            sourceType = PointHistorySourceType.ADMIN_GRANT,
                            sourceId = adminUserId,
                        ),
                    )
                    results += AdminAssetGrantItemResult(
                        userId = userId,
                        grantedAmount = grantedAmount,
                        balanceAfter = user.point,
                    )
                }
            }
            totalGrantedAmount += grantedAmount
        }

        log.info(
            "admin_asset_granted adminUserId={} assetType={} userCount={} totalAmount={}",
            adminUserId,
            request.assetType,
            results.size,
            totalGrantedAmount,
        )

        return AdminAssetGrantResponse(
            grantedUserCount = results.size,
            totalGrantedAmount = totalGrantedAmount,
            results = results,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminAssetGrantService::class.java)
    }
}
