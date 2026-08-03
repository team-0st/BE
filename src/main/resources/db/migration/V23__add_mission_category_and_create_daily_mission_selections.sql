alter table missions
    add column mission_category varchar(20) not null default 'GENERAL';

create table daily_mission_selections (
    id bigint not null auto_increment,
    selected_date date not null,
    mission_id bigint not null,
    mission_category varchar(20) not null,
    display_order int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_daily_mission_sel_mission_id foreign key (mission_id) references missions (id),
    constraint uq_daily_mission_sel_date_mission unique (selected_date, mission_id),
    constraint uq_daily_mission_sel_date_cat_order unique (selected_date, mission_category, display_order),
    constraint chk_daily_mission_sel_order_positive check (display_order >= 1)
);
