-- 전화번호 없는 미완료(가짜) 유저 정리
-- 대상: 2,3,4,6,7,8,10,11,12,13,14,15 (phone IS NULL + onboarding 미완료만 삭제)
-- H2(MySQL mode) 호환: DELETE JOIN 대신 subquery 사용

UPDATE tester_links
SET updated_by_user_id = NULL
WHERE updated_by_user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM community_mission_proof_images
WHERE community_mission_proof_id IN (
    SELECT id FROM community_mission_proofs
    WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15)
);

DELETE FROM community_mission_proofs
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM community_mission_completions
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM mission_completions
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM soup_reward_ingredients
WHERE soup_id IN (
    SELECT id FROM soups
    WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15)
);

DELETE FROM soups
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM gachas
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM user_unlocked_recipes
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM ingredient_histories
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM user_ingredients
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM check_ins
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM eco_jam_histories
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM point_histories
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM refresh_tokens
WHERE user_id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);

DELETE FROM users
WHERE id IN (2, 3, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15)
  AND phone_number IS NULL
  AND onboarding_completed = 0;
