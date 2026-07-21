package com.zerost.api.profile.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.profile.presentation.dto.UpdateProfileCharacterResponse
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileCharacterCommandService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun updateProfileCharacter(
        userId: Long,
        profileCharacterCode: String,
    ): UpdateProfileCharacterResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val character = ProfileCharacterCode.from(profileCharacterCode)
            ?: throw BusinessException(ErrorCode.INVALID_PROFILE_CHARACTER_CODE)

        user.changeProfileCharacter(character)

        return UpdateProfileCharacterResponse(
            userId = requireNotNull(user.id),
            profileCharacterCode = character.name,
        )
    }
}
