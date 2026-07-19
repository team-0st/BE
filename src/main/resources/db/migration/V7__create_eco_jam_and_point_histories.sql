create table eco_jam_histories (
    id bigint not null auto_increment,
    user_id bigint not null,
    amount int not null,
    source_type varchar(30) not null,
    source_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_eco_jam_histories_user_id foreign key (user_id) references users (id)
);

create index idx_eco_jam_histories_user_id_created_at
    on eco_jam_histories (user_id, created_at desc);

create table point_histories (
    id bigint not null auto_increment,
    user_id bigint not null,
    amount int not null,
    source_type varchar(30) not null,
    source_id bigint not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_point_histories_user_id foreign key (user_id) references users (id)
);

create index idx_point_histories_user_id_created_at
    on point_histories (user_id, created_at desc);
