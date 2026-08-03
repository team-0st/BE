create table community_mission_rewards (
    id bigint not null auto_increment,
    community_mission_id bigint not null,
    reward_type varchar(30) not null,
    ingredient_type varchar(20) null,
    quantity int not null default 0,
    eco_jam_amount int not null default 0,
    reward_order int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_community_mission_rewards_mission_id foreign key (community_mission_id) references community_missions (id),
    constraint chk_community_mission_rewards_quantity_non_negative check (quantity >= 0),
    constraint chk_community_mission_rewards_eco_jam_amount_non_negative check (eco_jam_amount >= 0),
    constraint chk_community_mission_rewards_reward_order_positive check (reward_order >= 1),
    constraint chk_community_mission_rewards_payload check (
        (reward_type = 'ECO_JAM' and ingredient_type is null and quantity = 0 and eco_jam_amount > 0) or
        (reward_type = 'INGREDIENT' and ingredient_type is not null and quantity > 0 and eco_jam_amount = 0)
    )
);
