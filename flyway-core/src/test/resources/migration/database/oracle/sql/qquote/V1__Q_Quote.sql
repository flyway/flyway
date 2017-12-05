--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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
