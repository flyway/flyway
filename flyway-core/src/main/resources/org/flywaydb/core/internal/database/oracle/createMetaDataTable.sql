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

CREATE TABLE "${schema}"."${table}" (
    "installed_rank" INT NOT NULL,
    "version" VARCHAR2(50),
    "description" VARCHAR2(200) NOT NULL,
    "type" VARCHAR2(20) NOT NULL,
    "script" VARCHAR2(1000) NOT NULL,
    "checksum" INT,
    "installed_by" VARCHAR2(100) NOT NULL,
    "installed_on" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "execution_time" INT NOT NULL,
    "success" NUMBER(1) NOT NULL
);
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_pk" PRIMARY KEY ("installed_rank");

CREATE INDEX "${schema}"."${table}_s_idx" ON "${schema}"."${table}" ("success");
