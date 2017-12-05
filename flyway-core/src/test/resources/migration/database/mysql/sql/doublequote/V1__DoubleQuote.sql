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

select "Hello 'quotes" from dual;
select "Hello 'quotes'" from dual;
select "Hello ''quotes" from dual;

select "Hellö '
multi-line
quotes
'" from dual;

select "Hello ';
multi-line
quotes
" from dual;

select "Hello
'multi-line'
quotes"
 from dual;

select 'Hello
"multi-line"
''quotes"
' from dual;
