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

CREATE TABLE CALENDAR (
    DAY NUMBER PRIMARY KEY,
    MONTH NUMBER,
    QUARTER NUMBER,
    YEAR NUMBER
);

CREATE DIMENSION CALENDAR_DIM
   LEVEL day         IS CALENDAR.DAY
   LEVEL month       IS CALENDAR.MONTH
   LEVEL quarter     IS CALENDAR.QUARTER
   LEVEL year        IS CALENDAR.YEAR
   HIERARCHY CALENDAR_ROLLUP    (
             day     CHILD OF
             month   CHILD OF
             quarter CHILD OF
             year
   );