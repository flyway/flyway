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

-- Execute only one DDL statement
CREATE SET TABLE financial.accts ,NO FALLBACK ,
     NO BEFORE JOURNAL,
     NO AFTER JOURNAL,
     CHECKSUM = DEFAULT,
     DEFAULT MERGEBLOCKRATIO
     (
      cust_id INTEGER NOT NULL,
      acct_type CHAR(2) CHARACTER SET LATIN NOT CASESPECIFIC NOT NULL,
      acct_nbr CHAR(16) CHARACTER SET LATIN NOT CASESPECIFIC NOT NULL,
      acct_start_date DATE FORMAT 'YYYYMMDD' NOT NULL,
      acct_end_date DATE FORMAT 'YYYYMMDD')
PRIMARY INDEX ( cust_id ,acct_type );
