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

select q'[Hello no quotes]' from dual;
select q'[Hello 'no quotes]' from dual;
select q'[Hello 'quotes']' from dual;
select q'(Hello 'quotes')' from dual;
select q'{Hello 'quotes'}' from dual;
select q'<Hello 'quotes'>' from dual;
select q'$Hello 'quotes'$' from dual;

select q'[Hello '
multi-line
quotes
']' from dual;

select q'(Hello '
multi-line
quotes
')' from dual;

select q'{Hello '
multi-line
quotes
'}' from dual;

select q'<Hello '
multi-line
quotes
'>' from dual;

select q'$Hello '
multi-line
quotes
'$' from dual;
