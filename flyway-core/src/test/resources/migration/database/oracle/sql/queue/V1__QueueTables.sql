--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

declare
  l_prefix VARCHAR2(131) := '"' || SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') || '".';
begin
--
---- Create Queue
DBMS_AQADM.CREATE_QUEUE_TABLE (queue_table  => l_prefix || 'TEST_QUEUE_MLG_QTAB', queue_payload_type   => 'SYS.AQ$_JMS_TEXT_MESSAGE');
DBMS_AQADM.CREATE_QUEUE (queue_name  => l_prefix || 'TEST_QUEUE_MLG', queue_table => l_prefix || 'TEST_QUEUE_MLG_QTAB');
DBMS_AQADM.START_QUEUE (l_prefix || 'TEST_QUEUE_MLG');
--

end;
/