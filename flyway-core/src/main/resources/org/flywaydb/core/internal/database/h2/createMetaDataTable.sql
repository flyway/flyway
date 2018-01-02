--
-- Copyright 2010-2018 Boxfuse GmbH
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
    "version" VARCHAR(50),
    "description" VARCHAR(200) NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "script" VARCHAR(1000) NOT NULL,
    "checksum" INT,
    "installed_by" VARCHAR(100) NOT NULL,
    "installed_on" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "execution_time" INT NOT NULL,
    "success" BOOLEAN NOT NULL
);
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_pk" PRIMARY KEY ("installed_rank");

CREATE INDEX "${schema}"."${table}_s_idx" ON "${schema}"."${table}" ("success");
