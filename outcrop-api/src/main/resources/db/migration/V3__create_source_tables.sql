create table source_class (
    id uuid primary key,
    analysis_id uuid not null references analysis (id) on delete cascade,
    package_name varchar(500) not null,
    simple_name varchar(200) not null,
    kind varchar(20) not null,
    file_path varchar(1000) not null
);

create table source_class_annotation (
    source_class_id uuid not null references source_class (id) on delete cascade,
    annotation varchar(200) not null
);

create table source_class_import (
    source_class_id uuid not null references source_class (id) on delete cascade,
    imported varchar(500) not null
);

create table source_method (
    id uuid primary key,
    source_class_id uuid not null references source_class (id) on delete cascade,
    name varchar(200) not null,
    return_type varchar(500) not null,
    parameter_count integer not null,
    start_line integer not null
);

create index idx_source_class_analysis on source_class (analysis_id);
create index idx_source_class_annotation on source_class_annotation (source_class_id);
create index idx_source_class_import on source_class_import (source_class_id);
create index idx_source_method_class on source_method (source_class_id);

alter table analysis add column class_count integer;
alter table analysis add column parse_failure_count integer;
