create table shops (
    id bigint not null auto_increment,
    name varchar(100) not null,
    description text null,
    image_url varchar(255) null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id)
);

create table users (
    id bigint not null auto_increment,
    device_id varchar(64) not null,
    nickname varchar(50) null,
    phone_number varchar(20) null,
    shop_id bigint null,
    eco_jam int not null default 0,
    onboarding_completed boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uk_users_device_id unique (device_id),
    constraint fk_users_shop_id foreign key (shop_id) references shops (id)
);
