create table community_mission_proof_requirements (
    id bigint not null auto_increment,
    community_mission_id bigint not null,
    proof_order int not null,
    title varchar(100) null,
    description text null,
    required_image_count int not null,
    required_day_offset int null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_community_mission_proof_requirements_mission_id foreign key (community_mission_id) references community_missions (id),
    constraint uq_community_mission_proof_requirements_id_mission_id unique (id, community_mission_id),
    constraint uq_community_mission_proof_requirements_mission_id_order unique (community_mission_id, proof_order),
    constraint chk_community_mission_proof_requirements_order_positive check (proof_order >= 1),
    constraint chk_community_mission_proof_requirements_required_image_count_positive check (required_image_count >= 1),
    constraint chk_community_mission_proof_requirements_required_image_count_max check (required_image_count <= 5),
    constraint chk_community_mission_proof_requirements_required_day_offset_non_negative check (required_day_offset is null or required_day_offset >= 0)
);

create table community_mission_proofs (
    id bigint not null auto_increment,
    community_mission_id bigint not null,
    proof_requirement_id bigint not null,
    user_id bigint not null,
    submitted_at timestamp not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_community_mission_proofs_mission_id foreign key (community_mission_id) references community_missions (id),
    constraint fk_community_mission_proofs_requirement_id_mission_id foreign key (proof_requirement_id, community_mission_id) references community_mission_proof_requirements (id, community_mission_id),
    constraint fk_community_mission_proofs_user_id foreign key (user_id) references users (id),
    constraint uq_community_mission_proofs_user_id_requirement_id unique (user_id, proof_requirement_id)
);

create table community_mission_proof_images (
    id bigint not null auto_increment,
    community_mission_proof_id bigint not null,
    image_key varchar(255) not null,
    image_order int not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id),
    constraint fk_community_mission_proof_images_proof_id foreign key (community_mission_proof_id) references community_mission_proofs (id),
    constraint uq_community_mission_proof_images_proof_id_order unique (community_mission_proof_id, image_order),
    constraint chk_community_mission_proof_images_order_positive check (image_order >= 1)
);
