alter table users
    add column profile_character_code varchar(50) null after password_hash;
