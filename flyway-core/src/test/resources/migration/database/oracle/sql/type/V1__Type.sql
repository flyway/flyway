--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TYPE full_name_type AS OBJECT
( FirstName       VARCHAR2(80),
  MiddleName      VARCHAR2(80),
  LastName        VARCHAR2(80) );
/

create or replace
TYPE "FULL_NAME_TYPE_ARRAY" AS TABLE of "FULL_NAME_TYPE";
/

CREATE TYPE full_mailing_address_type AS OBJECT
( full_name    full_name_type,
  Street       VARCHAR2(80),
  City         VARCHAR2(80),
  State        CHAR(2));
/

CREATE TABLE customer (
  full_address  full_mailing_address_type
);

INSERT INTO customer VALUES (
  full_mailing_address_type(
    full_name_type('John', 'F', 'Kennedy'),
    'Whitehouse plaza',
    'Washington',
    'DC'
  )
);

create or replace
TYPE FACT_ROW IS OBJECT (
    FACT_TIME      TIMESTAMP,
    DIMENSION_VALUE VARCHAR2(4000 BYTE),
    MEASURE_VALUE   NUMBER,
    CONSTRUCTOR FUNCTION FACT_ROW RETURN SELF AS RESULT
  );
/

create or replace
TYPE BODY  FACT_ROW AS
    CONSTRUCTOR FUNCTION FACT_ROW RETURN SELF AS RESULT IS
    BEGIN
      RETURN;
    END;
  END;
/
