--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE OR REPLACE TYPE my_tab_t AS TABLE OF VARCHAR2(30);
/
CREATE TABLE nested_table (id NUMBER, col1 my_tab_t)
       NESTED TABLE col1 STORE AS col1_tab;

INSERT INTO nested_table VALUES (1, my_tab_t('A'));
INSERT INTO nested_table VALUES (2, my_tab_t('B', 'C'));
INSERT INTO nested_table VALUES (3, my_tab_t('D', 'E', 'F'));