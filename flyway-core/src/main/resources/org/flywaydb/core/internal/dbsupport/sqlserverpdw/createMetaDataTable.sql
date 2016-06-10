--
-- Copyright 2010-2016 Boxfuse GmbH
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
IF XACT_STATE() = 1 COMMIT 

CREATE TABLE [dbo].[dbresult_migration] (  
  [installed_rank] int NOT NULL,     
  [version] nvarchar(50) COLLATE Latin1_General_100_CI_AS_KS_WS NULL,     
  [description] nvarchar(200) COLLATE Latin1_General_100_CI_AS_KS_WS NULL,     
  [type] nvarchar(20) COLLATE Latin1_General_100_CI_AS_KS_WS NOT NULL,     
  [script] nvarchar(1000) COLLATE Latin1_General_100_CI_AS_KS_WS NOT NULL,     
  [checksum] int NULL,     
  [installed_by] nvarchar(100) COLLATE Latin1_General_100_CI_AS_KS_WS NOT NULL,     
  [installed_on] datetime NULL,     
  [execution_time] int NOT NULL,     
  [success] bit NOT NULL
);
