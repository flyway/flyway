-- Create overall database
IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = '${DBNAME}')
   BEGIN
    PRINT 'Creating ${DBNAME} Database'
    CREATE DATABASE ${DBNAME}
   END
ELSE
    PRINT '${DBNAME} Database Exists'
GO