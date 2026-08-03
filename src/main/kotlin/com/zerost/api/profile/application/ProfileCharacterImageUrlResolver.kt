package com.zerost.api.profile.application

import com.zerost.api.common.config.PublicAssetsProperties
import com.zerost.api.user.domain.ProfileCharacterCode
import org.springframework.stereotype.Component

@Component
class ProfileCharacterImageUrlResolver(
    private val publicAssetsProperties: PublicAssetsProperties,
) {

    fun resolveOrNull(profileCharacterCode: ProfileCharacterCode?): String? {
        return profileCharacterCode?.let { resolve(it) }
    }

    fun resolve(profileCharacterCode: ProfileCharacterCode): String {
        val baseUrl = publicAssetsProperties.baseUrl.trimEnd('/')
        return "$baseUrl/profile-characters/${profileCharacterCode.fileName}"
    }
}
