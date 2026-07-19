create table recipes (
    id bigint not null auto_increment,
    name varchar(100) not null,
    type varchar(20) not null,
    slot_count int not null,
    is_hidden boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id)
);

create table recipe_ingredients (
    id bigint not null auto_increment,
    recipe_id bigint not null,
    ingredient_id bigint not null,
    slot_order int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_recipe_ingredients_recipe_id foreign key (recipe_id) references recipes (id),
    constraint fk_recipe_ingredients_ingredient_id foreign key (ingredient_id) references ingredients (id),
    constraint uk_recipe_ingredients_recipe_id_slot_order unique (recipe_id, slot_order)
);

create table soups (
    id bigint not null auto_increment,
    user_id bigint not null,
    recipe_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_soups_user_id foreign key (user_id) references users (id),
    constraint fk_soups_recipe_id foreign key (recipe_id) references recipes (id)
);
