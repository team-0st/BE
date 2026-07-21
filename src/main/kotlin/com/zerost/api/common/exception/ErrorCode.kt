package com.zerost.api.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String,
) {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."),
    DEVICE_ID_HEADER_MISSING(HttpStatus.BAD_REQUEST, "DEVICE_ID_HEADER_MISSING", "X-Device-Id 헤더는 필수입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE", "허용되지 않는 Accept 요청입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 Content-Type 입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "등록된 유저를 찾을 수 없습니다."),
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOP_NOT_FOUND", "선택한 상점을 찾을 수 없습니다."),
    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "오늘은 이미 출석했습니다."),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INGREDIENT_NOT_FOUND", "재료 정보를 찾을 수 없습니다."),

    // MISSION
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_NOT_FOUND", "미션 정보를 찾을 수 없습니다."),
    INVALID_MISSION_REWARD_POOL(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_MISSION_REWARD_POOL", "미션 보상 재료 설정이 올바르지 않습니다."),
    MISSION_UNDER_REVIEW(HttpStatus.CONFLICT, "MISSION_UNDER_REVIEW", "오늘 제출한 미션이 아직 검수 중입니다."),
    MISSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MISSION_ALREADY_COMPLETED", "오늘 제출한 미션이 이미 승인되었습니다."),
    MISSION_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_COMPLETION_NOT_FOUND", "미션 인증 정보를 찾을 수 없습니다."),
    INVALID_MISSION_REVIEW_STATUS(HttpStatus.CONFLICT, "INVALID_MISSION_REVIEW_STATUS", "검수할 수 없는 미션 인증 상태입니다."),

    // COMMUNITY MISSION
    COMMUNITY_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_MISSION_NOT_FOUND", "공동 미션 정보를 찾을 수 없습니다."),
    COMMUNITY_MISSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "COMMUNITY_MISSION_ALREADY_COMPLETED", "이미 완료한 공동 미션입니다."),
    COMMUNITY_MISSION_NOT_UNLOCKED(HttpStatus.CONFLICT, "COMMUNITY_MISSION_NOT_UNLOCKED", "아직 해금되지 않은 공동 미션입니다."),
    COMMUNITY_MISSION_ONBOARDING_REQUIRED(HttpStatus.CONFLICT, "COMMUNITY_MISSION_ONBOARDING_REQUIRED", "온보딩을 완료한 뒤 공동 미션을 완료할 수 있습니다."),

    // SOUP
    SOUP_NOT_FOUND(HttpStatus.NOT_FOUND, "SOUP_NOT_FOUND", "스프 제작 정보를 찾을 수 없습니다."),
    INVALID_SOUP_SLOT_COUNT(HttpStatus.BAD_REQUEST, "INVALID_SOUP_SLOT_COUNT", "스프 제작 재료 수가 올바르지 않습니다."),
    SOUP_RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "SOUP_RECIPE_NOT_FOUND", "일치하는 스프 레시피를 찾을 수 없습니다."),
    INSUFFICIENT_INGREDIENT_QUANTITY(HttpStatus.CONFLICT, "INSUFFICIENT_INGREDIENT_QUANTITY", "보유 재료 수량이 부족합니다."),
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "INSUFFICIENT_POINT", "보유 포인트가 부족합니다."),
    SOUP_REROLL_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SOUP_REROLL_ALREADY_COMPLETED", "이미 리롤을 완료한 스프입니다."),
    SOUP_REROLL_NOT_AVAILABLE(HttpStatus.CONFLICT, "SOUP_REROLL_NOT_AVAILABLE", "현재 보상 등급에서는 리롤을 진행할 수 없습니다."),
    SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE(HttpStatus.CONFLICT, "SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE", "기존 보상을 회수할 수 없어 리롤을 진행할 수 없습니다."),

    // RECIPE
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECIPE_NOT_FOUND", "레시피 정보를 찾을 수 없습니다."),
    ALL_HIDDEN_RECIPES_ALREADY_UNLOCKED(HttpStatus.CONFLICT, "ALL_HIDDEN_RECIPES_ALREADY_UNLOCKED", "해금 가능한 희귀 레시피를 모두 획득했습니다."),

    // GACHA
    INSUFFICIENT_ECO_JAM(HttpStatus.CONFLICT, "INSUFFICIENT_ECO_JAM", "보유 에코잼이 부족합니다."),
    GACHA_REWARD_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "GACHA_REWARD_POLICY_NOT_FOUND", "활성화된 가챠 보상 정책을 찾을 수 없습니다."),
    INVALID_GACHA_REWARD_POLICY(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_GACHA_REWARD_POLICY", "가챠 보상 정책이 올바르지 않습니다."),

    // FILE
    INVALID_FILE_KEY(HttpStatus.BAD_REQUEST, "INVALID_FILE_KEY", "유효하지 않은 업로드 파일 키입니다."),
    UPLOADED_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOADED_FILE_NOT_FOUND", "업로드한 파일을 찾을 수 없습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "빈 파일은 업로드할 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_SIZE_EXCEEDED", "파일 크기 제한을 초과했습니다."),
}
