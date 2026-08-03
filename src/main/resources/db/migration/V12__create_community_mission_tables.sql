create table community_missions (
    id bigint not null auto_increment,
    title varchar(100) not null,
    description text null,
    difficulty varchar(20) not null,
    stage int not null,
    target_ratio decimal(5,2) not null,
    image_url varchar(255) null,
    is_active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uq_community_missions_difficulty_stage unique (difficulty, stage),
    constraint chk_community_missions_stage check (stage >= 1),
    constraint chk_community_missions_target_ratio check (target_ratio >= 0 and target_ratio <= 100)
);

create table community_mission_completions (
    id bigint not null auto_increment,
    community_mission_id bigint not null,
    user_id bigint not null,
    completed_at timestamp not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uq_community_mission_completions_mission_id_user_id unique (community_mission_id, user_id),
    constraint fk_community_mission_completions_mission_id foreign key (community_mission_id) references community_missions (id),
    constraint fk_community_mission_completions_user_id foreign key (user_id) references users (id)
);
