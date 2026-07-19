create table ingredient_histories (
    id bigint not null auto_increment,
    user_id bigint not null,
    ingredient_id bigint not null,
    amount int not null,
    source_type varchar(30) not null,
    source_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_ingredient_histories_user_id foreign key (user_id) references users (id),
    constraint fk_ingredient_histories_ingredient_id foreign key (ingredient_id) references ingredients (id)
);

create index idx_ingredient_histories_user_id_created_at
    on ingredient_histories (user_id, created_at desc, id desc);
