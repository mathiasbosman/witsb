create table items
(
    id       uuid
        constraint pk_items primary key,
    name     varchar(255) not null,
    uploaded timestamp    not null,
    created  timestamp    not null,
    updated  timestamp    not null
);