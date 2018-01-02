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

CREATE TABLE EMPLOYEE(
  ID NUMBER,
  DEPT_ID NUMBER,
  COUNTRY_ID NUMBER
);

DECLARE
  l_prefix VARCHAR2(131) := '"' || SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') || '".';
BEGIN
  DBMS_RULE_ADM.CREATE_EVALUATION_CONTEXT(
    evaluation_context_name => l_prefix || 'TEST_EVAL_CTX1',
    table_aliases           => SYS.RE$TABLE_ALIAS_LIST(SYS.RE$TABLE_ALIAS('emp', l_prefix || 'employee')));

  DBMS_RULE_ADM.CREATE_EVALUATION_CONTEXT(
    evaluation_context_name => l_prefix || 'TEST_EVAL_CTX2',
    table_aliases           => SYS.RE$TABLE_ALIAS_LIST(SYS.RE$TABLE_ALIAS('emp', l_prefix || 'employee')));

  DBMS_RULE_ADM.CREATE_RULE(
    rule_name          => l_prefix || 'TEST_RULE1',
    condition          => 'emp.dept_id = 30',
    evaluation_context => l_prefix || 'TEST_EVAL_CTX1');

  DBMS_RULE_ADM.CREATE_RULE(
    rule_name          => l_prefix || 'TEST_RULE2',
    condition          => 'emp.country_id = 10');

  DBMS_RULE_ADM.CREATE_RULE_SET(
    rule_set_name      => l_prefix || 'TEST_RULE_SET',
    evaluation_context => l_prefix || 'TEST_EVAL_CTX2');

  DBMS_RULE_ADM.ADD_RULE(
   rule_name          => l_prefix || 'TEST_RULE1',
   rule_set_name      => l_prefix || 'TEST_RULE_SET');

  DBMS_RULE_ADM.ADD_RULE(
   rule_name          => l_prefix || 'TEST_RULE2',
   rule_set_name      => l_prefix || 'TEST_RULE_SET');
END;
/
