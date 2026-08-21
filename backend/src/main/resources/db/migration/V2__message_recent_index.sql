CREATE INDEX idx_message_chat_recent
    ON message (chat_id, sent_at DESC, id DESC);
