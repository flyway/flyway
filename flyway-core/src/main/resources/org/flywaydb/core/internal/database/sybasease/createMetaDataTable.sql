--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
--

CREATE TABLE ${table} (
    installed_rank INT NOT NULL,
    version VARCHAR(50) NULL,
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT NULL,
    installed_by VARCHAR(100) NOT NULL,
    installed_on datetime DEFAULT getDate() NOT NULL,
    execution_time INT NOT NULL,
    success decimal NOT NULL,
    PRIMARY KEY (installed_rank)
)
lock datarows on 'default'
go

CREATE INDEX ${table}_s_idx ON ${table} (success)
go

