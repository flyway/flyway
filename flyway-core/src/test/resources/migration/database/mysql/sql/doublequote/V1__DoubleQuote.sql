--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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
