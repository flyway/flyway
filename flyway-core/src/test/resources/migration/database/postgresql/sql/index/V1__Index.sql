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

CREATE TABLE cities (
    name            text,
    population      float,
    altitude        int     -- in feet
);

CREATE INDEX CONCURRENTLY cities_idx ON cities(name, population, altitude);