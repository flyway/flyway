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

CREATE RULE range_rule
AS
@range>= $1000 AND @range <$20000;
go

CREATE RULE list_rule
AS
@list IN ('1389', '0736', '0877');
GO

CREATE RULE pattern_rule
AS
@value LIKE '__-%[0-9]';
GO

CREATE DEFAULT phonedflt AS 'unknown';