alter table users
    add column password_hash varchar(255) null after phone_number;

alter table users
    add constraint uk_users_phone_number unique (phone_number);

alter table users
    add constraint uk_users_nickname unique (nickname);
