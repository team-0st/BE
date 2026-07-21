package com.zerost.api.profile.application

import com.zerost.api.profile.presentation.dto.ProfileCharacterResponse
import com.zerost.api.user.domain.ProfileCharacterCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileCharacterQueryService {

    @Transactional(readOnly = true)
    fun getProfileCharacters(): List<ProfileCharacterResponse> {
        return ProfileCharacterCode.entries.map { profileCharacter ->
            ProfileCharacterResponse(
                code = profileCharacter.name,
                name = profileCharacter.displayName,
                description = profileCharacter.description,
            )
        }
    }
}
