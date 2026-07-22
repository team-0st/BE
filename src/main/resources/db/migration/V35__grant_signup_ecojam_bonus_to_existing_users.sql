-- 일회성: 기존 유저(가입 보너스 미지급)에게 에코잼 +500
UPDATE users u
SET eco_jam = eco_jam + 500
WHERE NOT EXISTS (
    SELECT 1
    FROM eco_jam_histories h
    WHERE h.user_id = u.id
      AND h.source_type = 'SIGNUP'
      AND h.amount = 500
);

INSERT INTO eco_jam_histories (user_id, amount, source_type, source_id, created_at, updated_at)
SELECT u.id,
       500,
       'SIGNUP',
       0,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM eco_jam_histories h
    WHERE h.user_id = u.id
      AND h.source_type = 'SIGNUP'
      AND h.amount = 500
);
