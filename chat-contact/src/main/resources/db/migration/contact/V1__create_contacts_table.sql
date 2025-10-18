CREATE TABLE contacts
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID NOT NULL,
    contact_user_id UUID NOT NULL,
    display_name    VARCHAR(255),
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (owner_id, contact_user_id)
);

CREATE INDEX idx_contacts_lookup ON contacts (owner_id, contact_user_id);