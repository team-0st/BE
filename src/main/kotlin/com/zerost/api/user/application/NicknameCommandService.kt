package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.dto.UpdateNicknameResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NicknameCommandService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun updateNickname(
        userId: Long,
        nickname: String,
    ): UpdateNicknameResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        validateNickname(userId = requireNotNull(user.id), nickname = nickname)
        try {
            user.changeNickname(nickname)
            userRepository.flush()
        } catch (_: DataIntegrityViolationException) {
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }

        return UpdateNicknameResponse(
            userId = requireNotNull(user.id),
            nickname = requireNotNull(user.nickname),
        )
    }

    private fun validateNickname(userId: Long, nickname: String) {
        val existingUser = userRepository.findByNickname(nickname).orElse(null) ?: return
        if (existingUser.id != userId) {
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
