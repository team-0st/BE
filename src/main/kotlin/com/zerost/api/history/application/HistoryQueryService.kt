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
    fun getEcoJamHistories(userId: Long): List<AssetHistoryResponse> {
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return ecoJamHistoryRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId)
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
    fun getPointHistories(userId: Long): List<AssetHistoryResponse> {
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return pointHistoryRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId)
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

}
