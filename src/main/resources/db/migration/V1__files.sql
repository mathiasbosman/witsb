CREATE TABLE files
(
    id         UUID         NOT NULL,
    reference  UUID         NOT NULL,
    filename   VARCHAR(255) NOT NULL,
    context    VARCHAR(255) NOT NULL,
    version    INT          NOT NULL,
    group_id   UUID         NOT NULL,
    locked BOOLEAN NOT NULL,
    created_on TIMESTAMP    NOT NULL,
    updated_on TIMESTAMP    NOT NULL
);

ALTER TABLE files
    ADD PRIMARY KEY (id);
ALTER TABLE files
    ADD CONSTRAINT UQ_PUBLIC_ID_CONTEXT UNIQUE (reference, context);
ALTER TABLE files
    ADD CONSTRAINT UQ_VERSION_GROUP UNIQUE (version, group_id);
CREATE INDEX IX_PUBLIC_ID ON files (reference);