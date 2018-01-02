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

CREATE TABLE orders (
  order_id    NUMBER PRIMARY KEY,
  order_date  DATE NOT NULL,
  customer_id NUMBER NOT NULL,
  shipper_id  NUMBER)
PARTITION BY RANGE (order_date) (
  PARTITION y1 VALUES LESS THAN (TO_DATE('01-JAN-2006', 'DD-MON-YYYY')),
  PARTITION y2 VALUES LESS THAN (TO_DATE('01-JAN-2007', 'DD-MON-YYYY')),
  PARTITION y3 VALUES LESS THAN (TO_DATE('01-JAN-2008', 'DD-MON-YYYY')));

CREATE TABLE order_items (
  order_id    NUMBER NOT NULL,
  product_id  NUMBER NOT NULL,
  price       NUMBER,
  quantity    NUMBER,
  CONSTRAINT order_items_fk FOREIGN KEY (order_id) REFERENCES orders)
PARTITION BY REFERENCE (order_items_fk);