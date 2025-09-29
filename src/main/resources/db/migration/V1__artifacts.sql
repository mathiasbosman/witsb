CREATE TABLE artifacts
(
    id            UUID         NOT NULL,
    reference     UUID         NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    context       VARCHAR(255) NOT NULL,
    version       INT          NOT NULL,
    group_id      UUID         NOT NULL,
    locked        BOOLEAN      NOT NULL,
    lock_group_id UUID,
    uploaded_on TIMESTAMP NOT NULL,
    created_on    TIMESTAMP    NOT NULL,
    updated_on    TIMESTAMP    NOT NULL
);

ALTER TABLE artifacts
    ADD PRIMARY KEY (id);
ALTER TABLE artifacts
    ADD CONSTRAINT UQ_PUBLIC_ID_CONTEXT UNIQUE (reference, context);
ALTER TABLE artifacts
    ADD CONSTRAINT UQ_VERSION_GROUP UNIQUE (version, group_id);
CREATE INDEX IX_PUBLIC_ID ON artifacts (reference);