CREATE TABLE IF NOT EXISTS rule_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    product_id UUID NOT NULL,
    product_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rule_query (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id UUID NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    arguments JSONB NOT NULL,
    negate BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    CONSTRAINT fk_rule_query_rule FOREIGN KEY (rule_id) REFERENCES rule_entity(id) ON DELETE CASCADE
);