CREATE TABLE chat_messages
(
    id          UUID PRIMARY KEY,
    text        TEXT,
    sender_id   UUID,
    receiver_id UUID,
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_messages_sender_id ON chat_messages (sender_id);
CREATE INDEX idx_chat_messages_receiver_id ON chat_messages (receiver_id);
CREATE INDEX idx_chat_messages_read_status ON chat_messages (receiver_id, read_at);