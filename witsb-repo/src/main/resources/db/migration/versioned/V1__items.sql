create table items
(
    id      uuid default random_uuid()
        constraint pk_items primary key,
    name    varchar(100) not null,
    created timestamp    not null,
    updated timestamp    not null
);