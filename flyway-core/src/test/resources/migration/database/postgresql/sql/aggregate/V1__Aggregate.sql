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

-- State transition function:
CREATE  FUNCTION create_select(acc text, instr text) RETURNS text AS $$
  BEGIN
    IF acc IS NULL OR acc = '' THEN
      RETURN replace(instr,'.','_') ;
    ELSE
      RETURN acc || ', ' || replace(instr,'.','_') ;
    END IF;
  END;
$$ LANGUAGE plpgsql;

-- Aggregate function
CREATE AGGREGATE textcat_all(
  basetype    = text,
  sfunc       = create_select,
  stype       = text,
  initcond    = ''
);