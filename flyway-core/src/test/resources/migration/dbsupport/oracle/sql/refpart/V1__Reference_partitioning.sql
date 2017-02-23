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