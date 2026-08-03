alter table community_missions
    add column succeeded_at timestamp null;

alter table community_mission_completions
    add column rewarded_at timestamp null;
