create table tester_links (
    id bigint not null,
    deep_link varchar(512) not null,
    deployment_id varchar(100) not null,
    updated_by_user_id bigint null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_tester_links_updated_by_user_id
        foreign key (updated_by_user_id) references users (id)
);
