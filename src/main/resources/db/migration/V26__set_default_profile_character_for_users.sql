update users
set profile_character_code = 'CARROT'
where profile_character_code is null;

alter table users
    modify column profile_character_code varchar(50) not null default 'CARROT';
