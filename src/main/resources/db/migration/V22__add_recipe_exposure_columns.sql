alter table recipes
    add column is_intro boolean not null default false;

alter table recipes
    add column is_weekly boolean not null default false;
