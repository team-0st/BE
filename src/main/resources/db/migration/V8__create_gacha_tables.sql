create table gacha_reward_policies (
    id bigint not null auto_increment,
    name varchar(100) not null,
    reward_type varchar(30) not null,
    probability decimal(5,2) not null,
    point_amount int not null default 0,
    eco_jam_amount int not null default 0,
    ingredient_id bigint null,
    ingredient_quantity int not null default 0,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_gacha_reward_policies_ingredient_id foreign key (ingredient_id) references ingredients (id)
);

create table gachas (
    id bigint not null auto_increment,
    user_id bigint not null,
    reward_policy_id bigint not null,
    cost_eco_jam int not null,
    result_type varchar(30) not null,
    result_point int not null default 0,
    result_eco_jam int not null default 0,
    result_ingredient_id bigint null,
    result_ingredient_quantity int not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_gachas_user_id foreign key (user_id) references users (id),
    constraint fk_gachas_reward_policy_id foreign key (reward_policy_id) references gacha_reward_policies (id),
    constraint fk_gachas_result_ingredient_id foreign key (result_ingredient_id) references ingredients (id)
);

create index idx_gachas_user_id_created_at
    on gachas (user_id, created_at desc, id desc);
