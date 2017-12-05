--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

alter database flyway_db_ms set single_user with rollback immediate
GO

alter database flyway_db_ms set multi_user
GO