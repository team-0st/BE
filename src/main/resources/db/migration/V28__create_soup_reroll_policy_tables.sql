create table soup_reroll_policy_groups (
    id bigint not null auto_increment,
    recipe_type varchar(20) not null,
    current_reward_grade varchar(30) not null,
    reroll_cost_eco_jam int not null,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uq_soup_reroll_policy_groups unique (recipe_type, current_reward_grade),
    constraint chk_soup_reroll_groups_cost_nonneg check (reroll_cost_eco_jam >= 0)
);

create table soup_reroll_policy_candidates (
    id bigint not null auto_increment,
    soup_reroll_policy_group_id bigint not null,
    next_reward_grade varchar(30) not null,
    probability decimal(5,2) not null,
    point_amount int not null default 0,
    eco_jam_amount int not null default 0,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_soup_reroll_candidates_group_id foreign key (soup_reroll_policy_group_id) references soup_reroll_policy_groups (id),
    constraint uq_soup_reroll_candidates_group_grade unique (soup_reroll_policy_group_id, next_reward_grade),
    constraint chk_soup_reroll_candidates_prob_pos check (probability > 0),
    constraint chk_soup_reroll_candidates_point_nonneg check (point_amount >= 0),
    constraint chk_soup_reroll_candidates_eco_nonneg check (eco_jam_amount >= 0)
);

create table soup_reroll_policy_ingredients (
    id bigint not null auto_increment,
    soup_reroll_policy_candidate_id bigint not null,
    selection_type varchar(30) not null,
    ingredient_id bigint null,
    ingredient_type varchar(20) null,
    quantity int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_soup_reroll_ingredients_candidate_id foreign key (soup_reroll_policy_candidate_id) references soup_reroll_policy_candidates (id),
    constraint fk_soup_reroll_ingredients_ingredient_id foreign key (ingredient_id) references ingredients (id),
    constraint chk_soup_reroll_ingredients_qty_pos check (quantity > 0),
    constraint chk_soup_reroll_ingredients_payload check (
        (selection_type = 'FIXED' and ingredient_id is not null and ingredient_type is null) or
        (selection_type = 'RANDOM_BY_TYPE' and ingredient_id is null and ingredient_type is not null)
    )
);
