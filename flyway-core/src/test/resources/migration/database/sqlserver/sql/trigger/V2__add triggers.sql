--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE TABLE CUSTOMER
(
  id                                                                                    int                identity,
  creaon_date                                                                           datetime2                  ,
  name                                                                                  varchar( 100 )             ,
  CONSTRAINT PK_CUSTOMER PRIMARY KEY( id )
)

GO

CREATE TRIGGER CUSTOMER_INSERT ON CUSTOMER AFTER
INSERT AS BEGIN
UPDATE
    CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id
END

GO
CREATE TRIGGER CUSTOMER_UPDATE ON CUSTOMER AFTER
UPDATE AS BEGIN
UPDATE
    CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id
END