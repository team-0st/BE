alter table community_mission_proofs
    add column status varchar(20) not null default 'PENDING' after user_id,
    add column reviewed_at timestamp null after submitted_at;
