--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE FUNCTION Test() RETURNS VOID AS $$
BEGIN
  RAISE WARNING 'This is a warning';
  RAISE EXCEPTION 'This is an error';
END;
$$ LANGUAGE plpgsql;

SELECT Test();