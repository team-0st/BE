create table refresh_tokens (
    id bigint not null auto_increment,
    user_id bigint not null,
    token_hash varchar(64) not null,
    expires_at timestamp not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint uk_refresh_tokens_token_hash unique (token_hash),
    constraint fk_refresh_tokens_user_id foreign key (user_id) references users (id)
);
