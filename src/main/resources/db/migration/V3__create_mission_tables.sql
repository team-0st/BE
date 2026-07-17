create table missions (
    id bigint not null auto_increment,
    title varchar(100) not null,
    description text null,
    image_url varchar(255) null,
    reward_ingredient_pool json not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id)
);

create table mission_completions (
    id bigint not null auto_increment,
    user_id bigint not null,
    mission_id bigint not null,
    photo_url varchar(255) not null,
    status varchar(20) not null,
    rewarded_ingredient_id bigint null,
    submitted_at timestamp not null,
    reviewed_at timestamp null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_mission_completions_user_id foreign key (user_id) references users (id),
    constraint fk_mission_completions_mission_id foreign key (mission_id) references missions (id),
    constraint fk_mission_completions_rewarded_ingredient_id foreign key (rewarded_ingredient_id) references ingredients (id)
);
