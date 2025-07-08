DROP TABLE IF EXISTS chat_context;
CREATE TABLE chat_context (
    chat_id VARCHAR(255) PRIMARY KEY,
    chat_history TEXT,
    chat_analysis TEXT,
    item_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS item_context (
    item_id VARCHAR(255) PRIMARY KEY,
    item_info TEXT
);