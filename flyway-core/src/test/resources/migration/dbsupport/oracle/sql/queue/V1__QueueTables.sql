begin
--
---- Queue erzeugen
DBMS_AQADM.CREATE_QUEUE_TABLE (queue_table  => 'TEST_QUEUE_MLG_QTAB', queue_payload_type   => 'SYS.AQ$_JMS_TEXT_MESSAGE');
DBMS_AQADM.CREATE_QUEUE (queue_name  => 'TEST_QUEUE_MLG', queue_table => 'TEST_QUEUE_MLG_QTAB');
DBMS_AQADM.START_QUEUE ('TEST_QUEUE_MLG');
--

end;
/