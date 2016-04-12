--
-- Copyright 2010-2016 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

begin
--
---- Create Queue
DBMS_AQADM.CREATE_QUEUE_TABLE (queue_table  => 'TEST_QUEUE_MLG_QTAB', queue_payload_type   => 'SYS.AQ$_JMS_TEXT_MESSAGE');
DBMS_AQADM.CREATE_QUEUE (queue_name  => 'TEST_QUEUE_MLG', queue_table => 'TEST_QUEUE_MLG_QTAB');
DBMS_AQADM.START_QUEUE ('TEST_QUEUE_MLG');
--

end;
/