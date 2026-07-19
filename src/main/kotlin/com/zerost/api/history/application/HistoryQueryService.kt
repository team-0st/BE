package com.zerost.api.history.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.history.presentation.dto.AssetHistoryResponse
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HistoryQueryService(
    private val userRepository: UserRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) {

    @Transactional(readOnly = true)
    fun getEcoJamHistories(deviceId: String): List<AssetHistoryResponse> {
        val userId = getUserId(deviceId)
        return ecoJamHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { history ->
                AssetHistoryResponse(
                    historyId = requireNotNull(history.id),
                    amount = history.amount,
                    sourceType = history.sourceType.name,
                    sourceId = history.sourceId,
                    createdAt = history.createdAt.toString(),
                )
            }
    }

    @Transactional(readOnly = true)
    fun getPointHistories(deviceId: String): List<AssetHistoryResponse> {
        val userId = getUserId(deviceId)
        return pointHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { history ->
                AssetHistoryResponse(
                    historyId = requireNotNull(history.id),
                    amount = history.amount,
                    sourceType = history.sourceType.name,
                    sourceId = history.sourceId,
                    createdAt = history.createdAt.toString(),
                )
            }
    }

    private fun getUserId(deviceId: String): Long {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        return requireNotNull(user.id)
    }
}
