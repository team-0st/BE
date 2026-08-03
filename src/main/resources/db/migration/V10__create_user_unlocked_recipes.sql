create table user_unlocked_recipes (
    id bigint not null auto_increment,
    user_id bigint not null,
    recipe_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uq_user_unlocked_recipes_user_id_recipe_id unique (user_id, recipe_id),
    constraint fk_user_unlocked_recipes_user_id foreign key (user_id) references users (id),
    constraint fk_user_unlocked_recipes_recipe_id foreign key (recipe_id) references recipes (id)
);
