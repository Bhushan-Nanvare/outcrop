create table analysis (
    id uuid primary key,
    repository_url varchar(500) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    started_at timestamptz,
    finished_at timestamptz,
    failure_reason varchar(1000)
);

create index idx_analysis_created_at on analysis (created_at desc);
