package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.dto.UpdateNicknameResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class NicknameCommandService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun updateNickname(
        userId: Long,
        nickname: String,
    ): UpdateNicknameResponse {
        log.info(
            "nickname_update_requested userId={} nickname={}",
            userId,
            nickname,
        )

        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        validateNickname(userId = requireNotNull(user.id), nickname = nickname)
        try {
            user.changeNickname(nickname)
            userRepository.flush()
        } catch (_: DataIntegrityViolationException) {
            log.warn(
                "nickname_update_failed reason=duplicate_nickname_race userId={} nickname={}",
                userId,
                nickname,
            )
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }

        val updatedUserId = requireNotNull(user.id)
        val updatedNickname = requireNotNull(user.nickname)
        registerAfterCommitLog {
            log.info(
                "nickname_updated userId={} nickname={}",
                updatedUserId,
                updatedNickname,
            )
        }

        return UpdateNicknameResponse(
            userId = updatedUserId,
            nickname = updatedNickname,
        )
    }

    private fun validateNickname(userId: Long, nickname: String) {
        val existingUser = userRepository.findByNickname(nickname).orElse(null) ?: return
        if (existingUser.id != userId) {
            log.warn(
                "nickname_update_failed reason=duplicate_nickname userId={} nickname={} existingUserId={}",
                userId,
                nickname,
                requireNotNull(existingUser.id),
            )
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }
    }

    private fun registerAfterCommitLog(action: () -> Unit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(NicknameCommandService::class.java)
    }
}
