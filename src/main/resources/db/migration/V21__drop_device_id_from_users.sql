alter table users
    drop index uk_users_device_id,
    drop column device_id;
