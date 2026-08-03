create table recipe_hints (
    id bigint not null auto_increment,
    recipe_id bigint not null,
    hint_level varchar(20) not null,
    content varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_recipe_hints_recipe_id foreign key (recipe_id) references recipes (id),
    constraint uq_recipe_hints_recipe_id_level unique (recipe_id, hint_level)
);
