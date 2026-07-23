-- 테스트·미완료 유저 정리: 16, 17(전화 없음 미완료), 18(API 스모크 테스트8056)
-- H2(MySQL mode) 호환: subquery 삭제

UPDATE tester_links
SET updated_by_user_id = NULL
WHERE updated_by_user_id IN (16, 17, 18);

DELETE FROM community_mission_proof_images
WHERE community_mission_proof_id IN (
    SELECT id FROM community_mission_proofs
    WHERE user_id IN (16, 17, 18)
);

DELETE FROM community_mission_proofs
WHERE user_id IN (16, 17, 18);

DELETE FROM community_mission_completions
WHERE user_id IN (16, 17, 18);

DELETE FROM mission_completions
WHERE user_id IN (16, 17, 18);

DELETE FROM soup_reward_ingredients
WHERE soup_id IN (
    SELECT id FROM soups
    WHERE user_id IN (16, 17, 18)
);

DELETE FROM soups
WHERE user_id IN (16, 17, 18);

DELETE FROM gachas
WHERE user_id IN (16, 17, 18);

DELETE FROM user_unlocked_recipes
WHERE user_id IN (16, 17, 18);

DELETE FROM ingredient_histories
WHERE user_id IN (16, 17, 18);

DELETE FROM user_ingredients
WHERE user_id IN (16, 17, 18);

DELETE FROM check_ins
WHERE user_id IN (16, 17, 18);

DELETE FROM eco_jam_histories
WHERE user_id IN (16, 17, 18);

DELETE FROM point_histories
WHERE user_id IN (16, 17, 18);

DELETE FROM refresh_tokens
WHERE user_id IN (16, 17, 18);

DELETE FROM users
WHERE id IN (16, 17, 18)
  AND (
      (phone_number IS NULL AND onboarding_completed = 0)
      OR phone_number = '010-0000-8056'
  );
