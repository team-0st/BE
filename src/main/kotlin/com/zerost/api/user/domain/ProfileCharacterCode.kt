package com.zerost.api.user.domain

enum class ProfileCharacterCode(
    val displayName: String,
    val description: String,
) {
    BASIC_1("기본 캐릭터 1", "선택형 프로필 기본 캐릭터 1"),
    BASIC_2("기본 캐릭터 2", "선택형 프로필 기본 캐릭터 2"),
    BASIC_3("기본 캐릭터 3", "선택형 프로필 기본 캐릭터 3"),
    BASIC_4("기본 캐릭터 4", "선택형 프로필 기본 캐릭터 4"),
    ;

    companion object {
        fun from(code: String): ProfileCharacterCode? {
            return entries.firstOrNull { it.name == code }
        }
    }
}
