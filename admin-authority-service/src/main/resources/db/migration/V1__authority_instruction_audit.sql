CREATE TABLE authority_instruction_audit (
    id              UUID PRIMARY KEY,
    instruction_type VARCHAR(64) NOT NULL,
    correlation_id   VARCHAR(128) NOT NULL,
    jurisdiction_id  VARCHAR(128),
    segment_ids      JSONB,
    payload_json     TEXT NOT NULL,
    map_version      BIGINT,
    external_ref     VARCHAR(256),
    status           VARCHAR(32) NOT NULL,
    error_detail     TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_authority_audit_segment ON authority_instruction_audit (instruction_type, created_at DESC);
CREATE INDEX idx_authority_audit_correlation ON authority_instruction_audit (correlation_id);
