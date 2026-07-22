create table soup_reward_policies (
    id bigint not null auto_increment,
    recipe_type varchar(20) not null,
    intro_only boolean not null default false,
    reward_grade varchar(30) not null,
    probability decimal(5,2) not null,
    point_amount int not null default 0,
    eco_jam_amount int not null default 0,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint chk_soup_reward_policies_probability_positive check (probability > 0),
    constraint chk_soup_reward_policies_point_amount_non_negative check (point_amount >= 0),
    constraint chk_soup_reward_policies_eco_jam_amount_non_negative check (eco_jam_amount >= 0)
);

create table soup_reward_policy_ingredients (
    id bigint not null auto_increment,
    soup_reward_policy_id bigint not null,
    selection_type varchar(30) not null,
    ingredient_id bigint null,
    ingredient_type varchar(20) null,
    quantity int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_soup_reward_policy_ingredients_policy_id foreign key (soup_reward_policy_id) references soup_reward_policies (id),
    constraint fk_soup_reward_policy_ingredients_ingredient_id foreign key (ingredient_id) references ingredients (id),
    constraint chk_soup_reward_policy_ingredients_quantity_positive check (quantity > 0),
    constraint chk_soup_reward_policy_ingredients_selection_payload check (
        (selection_type = 'FIXED' and ingredient_id is not null and ingredient_type is null) or
        (selection_type = 'RANDOM_BY_TYPE' and ingredient_id is null and ingredient_type is not null)
    )
);
