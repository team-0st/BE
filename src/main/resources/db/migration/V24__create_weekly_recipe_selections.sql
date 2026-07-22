create table weekly_recipe_selections (
    id bigint not null auto_increment,
    week_start_date date not null,
    recipe_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_weekly_recipe_sel_recipe_id foreign key (recipe_id) references recipes (id),
    constraint uq_weekly_recipe_sel_week_start_date unique (week_start_date)
);
