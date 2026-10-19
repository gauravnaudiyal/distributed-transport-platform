create table if not exists audit_records (
    id uuid primary key,
    event_type varchar(64) not null,
    booking_id uuid null,
    segment_id varchar(128) null,
    region_id varchar(64) not null,
    source_service varchar(128) not null,
    correlation_id varchar(128) null,
    map_version varchar(64) null,
    event_timestamp timestamp with time zone not null,
    created_at timestamp with time zone not null,
    payload_json text not null
);

create index if not exists idx_audit_records_booking_id
    on audit_records (booking_id, event_timestamp);

create index if not exists idx_audit_records_segment_id
    on audit_records (segment_id, event_timestamp);

create index if not exists idx_audit_records_region_id
    on audit_records (region_id, event_timestamp);
