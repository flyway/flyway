--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE address (
    id bigint NOT NULL,
    address character varying(256) NOT NULL
);

INSERT INTO address VALUES (1, '1. first
2. second');

COMMENT ON COLUMN address.address IS 'ATIVO = 1;
CONCLUIDO = 2;
CANCELADO = 0;';

