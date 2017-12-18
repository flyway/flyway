USE [flyway_db_ms]
GO

CREATE TABLE [table1](
        [owner_name] NVARCHAR(100) NOT NULL,
        [tenant_name] NVARCHAR(36) NOT NULL,
	CONSTRAINT [pk_tab1] PRIMARY KEY NONCLUSTERED ([owner_name], [tenant_name])
)
GO

ALTER TABLE [table1] ADD  DEFAULT ('guest') FOR [tenant_name]
GO

-- use other db

USE [flyway_db_jtds]
GO

CREATE TABLE [tab2] (
    [area_name] NVARCHAR(128) NOT NULL
);
go