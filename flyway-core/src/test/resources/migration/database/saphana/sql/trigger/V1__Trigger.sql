--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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