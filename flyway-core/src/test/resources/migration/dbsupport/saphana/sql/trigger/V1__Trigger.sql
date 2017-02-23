--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE TABLE TARGET ( A INT);
 CREATE TABLE control_tab(id INT PRIMARY KEY, name VARCHAR(30), payment INT);
 CREATE TABLE message_box(message VARCHAR(200), log_time TIMESTAMP);

 CREATE TRIGGER TEST_TRIGGER_FOR_INSERT
 AFTER INSERT ON TARGET
 BEGIN
     DECLARE v_id        INT := 0;
     DECLARE v_name      VARCHAR(20) := '';
     DECLARE v_pay       INT := 0;
     DECLARE v_msg       VARCHAR(200) := '';
     DELETE FROM message_box;
     FOR v_id IN 100 .. 103 DO
         SELECT name, payment INTO v_name, v_pay FROM control_tab WHERE id = :v_id;
         v_msg := :v_name || ' has ' || TO_CHAR(:v_pay);
         INSERT INTO message_box VALUES (:v_msg, CURRENT_TIMESTAMP);
     END FOR;
 END;

 CREATE TABLE SAMPLE ( A INT);
 CREATE TRIGGER TEST_TRIGGER_WHILE_UPDATE
 AFTER UPDATE ON TARGET
 BEGIN
     DECLARE found INT := 1;
     DECLARE val INT := 1;
     WHILE :found <> 0 DO
         SELECT count(*) INTO found FROM sample WHERE a = :val;
         IF :found = 0 THEN
             INSERT INTO sample VALUES(:val);
         END IF;
         val := :val + 1;
     END WHILE;
 END;