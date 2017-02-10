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

CREATE TABLE PRJ_IMPORTTABLES_CONFIG (
  TABLE_REALNAME VARCHAR2(50) NOT NULL,
  TABLE_SYNONYMNAME VARCHAR2(50) NOT NULL,
  VERSION_NUMBER NUMBER(4) NOT NULL,
  JOBNAME VARCHAR2(40) NOT NULL,
  ACTIVE_FLG NUMBER(1) NOT NULL,
  INSERTED_AT DATE NOT NULL,
  INSERTED_BY VARCHAR2(30) NOT NULL,
  UPDATED_AT DATE,
  UPDATED_BY VARCHAR2(30)
 );

COMMENT ON TABLE PRJ_IMPORTTABLES_CONFIG IS 'Stores configuration details which are required for import process of PRJ tables from DWH into PRJDB';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.TABLE_REALNAME IS 'Real name of PRJ-table in database';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.TABLE_SYNONYMNAME IS 'Synonym the application is using for table access';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.VERSION_NUMBER IS 'Version number';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.JOBNAME IS 'Name of import job which is loading data into table';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.ACTIVE_FLG IS 'Flag which table is active (synonym points to): 1 = active; 0 = incative; (only one "active record" per synonym is allowed)';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.INSERTED_AT IS 'Date and time when data were inserted';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.INSERTED_BY IS 'Name of user who inserted data';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.UPDATED_AT IS 'Date and time when data were changed';
COMMENT ON COLUMN PRJ_IMPORTTABLES_CONFIG.UPDATED_BY IS 'Name of user or process changing data';


--
-- Non Foreign Key Constraints for Table PRJ_IMPORTTABLES_CONFIG
--
ALTER TABLE PRJ_IMPORTTABLES_CONFIG ADD
  CONSTRAINT PRJ_ITC_PK PRIMARY KEY (TABLE_REALNAME);


--
-- Non Foreign Key Constraints for Table PRJ_IMPORTTABLES_CONFIG
--
ALTER TABLE PRJ_IMPORTTABLES_CONFIG ADD
  CONSTRAINT PRJ_ITC_ACTIVE_FLG_CON CHECK (ACTIVE_FLG IN (0,1));


--
-- Create Index
--
CREATE UNIQUE INDEX PRJ_ITC_ACTFLG_TABSYN_UI ON PRJ_IMPORTTABLES_CONFIG
(DECODE (ACTIVE_FLG, 1, TABLE_SYNONYMNAME, NULL));


--
-- Create Table
--
CREATE TABLE PRJ_IMPORTJOB_LOG
(
  IMPORTJOBLOG_ID NUMBER(11) NOT NULL,
  JOBNAME VARCHAR2(40) NOT NULL,
  IMPORTFILE VARCHAR2(50) NOT NULL,
  TABLENAME VARCHAR2(50) NOT NULL,
  TOTAL_OF_RECORDS NUMBER(9) NOT NULL,
  SYNONYM_SWITCHED_FLG NUMBER(1) NOT NULL,
  IMPORT_STATUS NUMBER(2) NOT NULL,
  IMPORTED_AT DATE NOT NULL,
  IMPORTED_BY VARCHAR2(30) NOT NULL,
  PROCESSED_AT DATE,
  PROCESSED_BY VARCHAR2(30)
);

COMMENT ON TABLE PRJ_IMPORTJOB_LOG IS 'In this table all import job for PRJ tables are logged';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.IMPORTJOBLOG_ID IS 'Importjoblog_ID: Primary Key';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.JOBNAME IS 'Name of importjob';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.IMPORTFILE IS 'Name of imported file';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.TOTAL_OF_RECORDS IS 'Total of records contained in the importfile';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.SYNONYM_SWITCHED_FLG IS 'Flag indicating if synonym for table was switched: 0 = not switched; 1 = switched;';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.IMPORT_STATUS IS 'Status of import: 0 = Job processing not started; 99 = Job processing succesfully ended; -99 = Job processing failed';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.IMPORTED_AT IS 'Date of import';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.IMPORTED_BY IS 'Who started the importjob';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.PROCESSED_AT IS 'Date when job is processed';
COMMENT ON COLUMN PRJ_IMPORTJOB_LOG.PROCESSED_BY IS 'Who processed the job';


--
-- Non Foreign Key Constraints for Table PRJ_IMPORTJOB_LOG
--
ALTER TABLE PRJ_IMPORTJOB_LOG ADD
  CONSTRAINT PRJ_IJL_PK PRIMARY KEY
 (IMPORTJOBLOG_ID);


--
-- Non Foreign Key Constraints for Table PRJ_IMPORTJOB_LOG
--
ALTER TABLE PRJ_IMPORTJOB_LOG ADD
  CONSTRAINT PRJ_IJL_SYN_SWITCHED_FLG_CON CHECK (SYNONYM_SWITCHED_FLG IN (0,1));


--
-- Non Foreign Key Constraints for Table PRJ_IMPORTJOB_LOG
--
ALTER TABLE PRJ_IMPORTJOB_LOG ADD
  CONSTRAINT PRJ_IJL_IMPORT_STATUS_CON CHECK (IMPORT_STATUS IN (0,-99,99));


--
-- Create Package
--
CREATE OR REPLACE PACKAGE PRJ_UTIL AS

/**
*/


/*
********************************************************************************
** EXCEPTION definitions
********************************************************************************
*/

/*
********************************************************************************
** TYPE AND SUBTYPE definitions
********************************************************************************
*/

/*
********************************************************************************
** CONSTANT definitions
********************************************************************************
*/

/*
********************************************************************************
** PROCEDURE and FUNCTION specifications
********************************************************************************
*/

/*
********************************************************************************
** PROCEDURE and FUNCTION specifications
********************************************************************************
*/
  /**
  ********************************************************************************
  ********************************************************************************
  */
  FUNCTION GetNextInactiveTable(picImpJob IN VARCHAR2)
     RETURN VARCHAR2;

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE TruncateImportTable (picTableName IN VARCHAR2);

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE InsertLogTable ( pinImportJobLogID IN PRJ_importjob_log.importjoblog_id%TYPE,
                             picJobName        IN PRJ_importjob_log.jobname%TYPE,
                             picImportFile     IN PRJ_importjob_log.importfile%TYPE,
                             picTableName      IN PRJ_importjob_log.tablename%TYPE,
                             pinTotalOfRecords IN PRJ_importjob_log.total_of_records%TYPE,
                             picImportedBy     IN PRJ_importjob_log.imported_by%TYPE);

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE ProcessAllImportJobs;

 /**
  */
  PROCEDURE CheckImportJob(pinImpJobID  IN NUMBER
                          ,pobValid     OUT BOOLEAN
                          ,pocTableName OUT VARCHAR2);

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE ProcessSingleImportJob(pinImpJobID IN NUMBER) ;
END PRJ_UTIL;
/

CREATE OR REPLACE PACKAGE BODY PRJ_UTIL AS
/**
*/


/*
********************************************************************************
** Global Variables
********************************************************************************
*/

/*
********************************************************************************
** Private CONSTANTs
********************************************************************************
*/

/*
********************************************************************************
** Private PROCEDUREs and FUNCTIONs
********************************************************************************
*/



/*
********************************************************************************
** Public PROCEDUREs and FUNCTIONs
********************************************************************************
*/

 /**
  ********************************************************************************
  ********************************************************************************
  */
  FUNCTION GetNextInactiveTable(picImpJob IN VARCHAR2) RETURN VARCHAR2 IS
      vcTableName     PRJ_importtables_config.table_realname%TYPE;
      vcActiveVersion PRJ_importtables_config.version_number%TYPE;
  BEGIN
        -- Get active version
        SELECT version_number
          INTO vcActiveVersion
          FROM PRJ_importtables_config
         WHERE jobname = picImpJob
           AND active_flg = 1;
        -- Get name of inactive table
        BEGIN
            -- Select name of inactive with proximate version number
            SELECT cic1.table_realname
              INTO vcTableName
              FROM PRJ_importtables_config cic1
             WHERE cic1.jobname = picImpJob
               AND cic1.version_number = (SELECT MIN(cic2.version_number)
                                            FROM PRJ_importtables_config cic2
                                           WHERE cic2.version_number > vcActiveVersion
                                             AND cic2.jobname = picImpJob);
        EXCEPTION WHEN NO_DATA_FOUND THEN
            -- active version is highest version number => get tablename with minimum version number
            SELECT cic1.table_realname
              INTO vcTableName
              FROM PRJ_importtables_config cic1
             WHERE cic1.jobname = picImpJob
               AND cic1.version_number = (SELECT MIN(cic2.version_number)
                                            FROM PRJ_importtables_config cic2
                                           WHERE cic2.active_flg <> 1
                                             AND cic2.jobname = picImpJob);
        END;
        -- Return name of table
        RETURN vcTableName;
  END GetNextInactiveTable;

 /**
  ********************************************************************************
  ********************************************************************************
  */

  PROCEDURE TruncateImportTable (picTableName IN VARCHAR2) IS
      vcSQL          VARCHAR2(4000);
  BEGIN
      -- Truncate given table
      vcSQL := 'TRUNCATE  TABLE '||picTableName;
      EXECUTE IMMEDIATE vcSQL;
  END TruncateImportTable;

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE InsertLogTable ( pinImportJobLogID IN PRJ_importjob_log.importjoblog_id%TYPE,
                             picJobName        IN PRJ_importjob_log.jobname%TYPE,
                             picImportFile     IN PRJ_importjob_log.importfile%TYPE,
                             picTableName      IN PRJ_importjob_log.tablename%TYPE,
                             pinTotalOfRecords IN PRJ_importjob_log.total_of_records%TYPE,
                             picImportedBy     IN PRJ_importjob_log.imported_by%TYPE) IS
  BEGIN
      -- Insert into table PRJ_IMPORTJOB_LOG
      INSERT
        INTO PRJ_importjob_log
           ( importjoblog_id, jobname, importfile, tablename, total_of_records, synonym_switched_flg
            ,import_status, imported_at, imported_by)
      VALUES
           ( pinImportJobLogID, picJobName, picImportFile, picTableName, pinTotalOfRecords, 0
            ,0, SYSDATE, picImportedBy);
      -- Commit insert
      COMMIT;
      --
  END InsertLogTable;

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE ProcessAllImportJobs IS
        CURSOR curImpJobLMS IS
            SELECT importjoblog_id
              FROM PRJ_importjob_log
             WHERE import_status = 0
             ORDER BY importjoblog_id ASC;
  BEGIN
      -- Process all new import jobs
      FOR recImpJobLMS IN curImpJobLMS
      LOOP
          ProcessSingleImportJob(recImpJobLMS.importjoblog_id);
      END LOOP;
  END ProcessAllImportJobs;

 /**
  ********************************************************************************
  ********************************************************************************
  */
  PROCEDURE CheckImportJob(pinImpJobID  IN NUMBER
                          ,pobValid     OUT BOOLEAN
                          ,pocTableName OUT VARCHAR2) IS
      vcTableName             PRJ_importjob_log.tablename%TYPE;
      vnTotalOfRecords        PRJ_importjob_log.total_of_records%TYPE;
      vnCountRecords          PRJ_importjob_log.total_of_records%TYPE;
      vnImportStatus          PRJ_importjob_log.import_status%TYPE;
      vnSynonymSwitchedFlg    PRJ_importjob_log.synonym_switched_flg%TYPE;
      vbValid                 BOOLEAN;
      vcSQL                   VARCHAR2(4000);
      -- cursor to find indexes of loaded tables
      CURSOR curIndex(picTableName IN PRJ_importjob_log.tablename%TYPE) IS
          SELECT index_name
            FROM user_indexes
           WHERE table_name = picTableName
             AND status != 'VALID';
  BEGIN
      -- Check if Importjob ID was provided
      IF pinImpJobID IS NULL THEN
          RAISE_APPLICATION_ERROR (-20001, 'Mandatory input parameter pinImpJobID is NULL') ;
      END IF;
      -- Get tablename and number of imported records from log table
      BEGIN
          SELECT tablename, total_of_records, import_status, synonym_switched_flg
            INTO vcTableName, vnTotalOfRecords , vnImportStatus, vnSynonymSwitchedFlg
            FROM PRJ_importjob_log
           WHERE importjoblog_id = pinImpJobID;
      EXCEPTION
          -- If import record not found return
          WHEN NO_DATA_FOUND THEN
              RAISE_APPLICATION_ERROR (-20002, 'No log record found for pinImpJobID=['||pinImpJobID||'] ');
      END;
      --  Check if record has correct status
      IF vnImportStatus <> 0 THEN
          RAISE_APPLICATION_ERROR (/*test*/-20003, 'Import job pinImpJobID=['||pinImpJobID||'] has wrong status ['||vnImportStatus||'] ');
      END IF;
      -- #6469 Check for unusable indexes and recreate
      FOR recIndex IN curIndex(vcTableName)
      LOOP
          EXECUTE IMMEDIATE 'ALTER INDEX '|| recIndex.index_name ||' REBUILD NOLOGGING';
      END LOOP;
      -- Determine total number of records in imported table
      vcSQL := 'SELECT count(*) FROM '||vcTableName;
      EXECUTE IMMEDIATE vcSQL
         INTO vnCountRecords;
      -- Compare number of records to check import process
      IF vnCountRecords <> vnTotalOfRecords THEN
          pobValid := FALSE;
          -- Import was not successful: update log table for import failure
          UPDATE PRJ_importjob_log
             SET import_status = -99
                 ,processed_at = SYSDATE
                 ,processed_by = USER
           WHERE importjoblog_id = pinImpJobID;
      ELSE
          pobValid := TRUE;
      END IF;
      -- Give the table name back
      pocTableName := vcTableName;
  END CheckImportJob;

 /**
  ********************************************************************************

  ********************************************************************************
  */
  PROCEDURE ProcessSingleImportJob(pinImpJobID IN NUMBER) IS
      vcTableName             PRJ_importjob_log.tablename%TYPE;
      vbSwitchSynonymFlg      BOOLEAN;
      vcSynonymName           PRJ_importtables_config.table_synonymname%TYPE;
      vcSQL                   VARCHAR2(4000);
  BEGIN
      CheckImportJob(pinImpJobID  => pinImpJobID
                    ,pobValid     => vbSwitchSynonymFlg
                    ,pocTableName => vcTableName);
      -- Switch synonym and update table PRJ_importtables_config
      IF vbSwitchSynonymFlg THEN
          -- Determine synonym
          SELECT table_synonymname
            INTO vcSynonymName
            FROM PRJ_importtables_config
           WHERE table_realname = vcTableName;
          -- Switch synonym
          vcSQL := 'CREATE OR REPLACE SYNONYM '||vcSynonymName||' FOR '||vcTableName;
          EXECUTE IMMEDIATE vcSQL;
          -- Update table PRJ_importtables_config
          UPDATE PRJ_importtables_config
             SET active_flg = 0
                 ,updated_at = SYSDATE
                 ,updated_by = USER
           WHERE table_synonymname = vcSynonymName
             AND active_flg = 1;
          UPDATE PRJ_importtables_config
             SET active_flg = 1
                 ,updated_at = SYSDATE
                 ,updated_by = USER
           WHERE table_realname = vcTableName;
          -- Update table PRJ_importjob_log
          UPDATE PRJ_importjob_log
             SET import_status = 99
                 ,synonym_switched_flg = 1
                 ,processed_at = SYSDATE
                 ,processed_by = USER
           WHERE importjoblog_id = pinImpJobID;
      END IF;
      -- Commit work
      COMMIT;
  END ProcessSingleImportJob;
END PRJ_UTIL;
/

