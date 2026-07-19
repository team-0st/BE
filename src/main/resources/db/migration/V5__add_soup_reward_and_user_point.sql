alter table users
    add column almang_point int not null default 0;

alter table soups
    add column reward_grade varchar(30) not null default 'CONSOLATION';

alter table soups
    add column reward_eco_jam int not null default 0;

alter table soups
    add column reward_almang_point int not null default 0;

create table soup_reward_ingredients (
    id bigint not null auto_increment,
    soup_id bigint not null,
    ingredient_id bigint not null,
    quantity int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_soup_reward_ingredients_soup_id foreign key (soup_id) references soups (id),
    constraint fk_soup_reward_ingredients_ingredient_id foreign key (ingredient_id) references ingredients (id)
);
