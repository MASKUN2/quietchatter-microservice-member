CREATE TABLE IF NOT EXISTS member
(
    id               UUID PRIMARY KEY,
    nickname         VARCHAR(255),
    role             VARCHAR(255),
    status           VARCHAR(255),
    provider         VARCHAR(255),
    provider_id      VARCHAR(255),
    created_at       TIMESTAMP,
    last_modified_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_member_provider_provider_id ON member (provider, provider_id);
