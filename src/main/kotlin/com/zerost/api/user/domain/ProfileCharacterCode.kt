package com.zerost.api.user.domain

enum class ProfileCharacterCode(
    val displayName: String,
    val description: String,
    val fileName: String,
) {
    BROCCOLI("브로콜리", "브로콜리 프로필 캐릭터", "broccoli.png"),
    CABBAGE("양배추", "양배추 프로필 캐릭터", "cabbage.png"),
    CARROT("당근", "당근 프로필 캐릭터", "carrot.png"),
    ECO_STAR("에코 스타", "에코 스타 프로필 캐릭터", "eco_star.png"),
    MUSHROOM("버섯", "버섯 프로필 캐릭터", "mushroom.png"),
    NATURE_SPROUT("자연의 새싹", "자연의 새싹 프로필 캐릭터", "nature_sprout.png"),
    ONION("양파", "양파 프로필 캐릭터", "onion.png"),
    PAPRIKA("파프리카", "파프리카 프로필 캐릭터", "paprika.png"),
    REFILL_CRYSTAL("리필 크리스탈", "리필 크리스탈 프로필 캐릭터", "refill_crystal.png"),
    TOMATO("토마토", "토마토 프로필 캐릭터", "tomato.png"),
    ;

    companion object {
        fun from(code: String): ProfileCharacterCode? {
            return entries.firstOrNull { it.name == code }
        }
    }
}
