create table ingredients (
    id bigint not null auto_increment,
    name varchar(50) not null,
    type varchar(20) not null,
    image_url varchar(255) null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id)
);

create table user_ingredients (
    id bigint not null auto_increment,
    user_id bigint not null,
    ingredient_id bigint not null,
    quantity int not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uk_user_ingredients_user_id_ingredient_id unique (user_id, ingredient_id),
    constraint fk_user_ingredients_user_id foreign key (user_id) references users (id),
    constraint fk_user_ingredients_ingredient_id foreign key (ingredient_id) references ingredients (id)
);

create table check_ins (
    id bigint not null auto_increment,
    user_id bigint not null,
    rewarded_ingredient_id bigint not null,
    checked_date date not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uk_check_ins_user_id_checked_date unique (user_id, checked_date),
    constraint fk_check_ins_user_id foreign key (user_id) references users (id),
    constraint fk_check_ins_rewarded_ingredient_id foreign key (rewarded_ingredient_id) references ingredients (id)
);
