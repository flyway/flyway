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

do $$
begin
	raise notice 'hello world';
end
$$;

do '
begin
	raise notice ''hello world'';
end';

do language plpgsql $$
begin
	raise notice 'hello world';
end
$$;