alter table community_mission_proofs
    add column status varchar(20) not null default 'PENDING';

alter table community_mission_proofs
    add column reviewed_at timestamp null;
