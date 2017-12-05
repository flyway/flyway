--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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