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

create or replace
PACKAGE AgingUtils
IS
  /* ----------------------------------
  NAME
  AgingUtils - create / drop partitions.

  DESCRIPTION

  NOTES
  load_defaults - procedure with default ranges.
  Below prefixTsName. All tablespaces will have name started with this prefix.

  Important:
  Fixes for NETRO schema.
	Run output from those queries.
SQL> select 'alter table '||table_name||' rename partition '||partition_name||' to '||replace(partition_name,'GG','G')||';' from user_tab_partitions where partition_name like '%GG%';
SQL> select 'alter index '||index_name||' rename partition '||partition_name||' to '||replace(partition_name,'GG','G')||';' from user_ind_partitions where partition_name like '%GG%';
----------------------------------  */

prefixTsName VARCHAR2(10) DEFAULT 'TNF';
versionString VARCHAR2(10) DEFAULT '5.12';

TYPE tp_prop IS TABLE OF VARCHAR2(5) INDEX BY VARCHAR2(100);


procedure load_defaults (prop OUT tp_prop);
function version return varchar2;

/**----------------------------------
  Add partition for table with p_date date
  INPUT VALUE:
  p_table_name : Table Name
  p_date:      Date for partition
  RETURN CODE:
  0 - success
  Number   - ORA error
  */

FUNCTION add_partition_for_table (
    p_table_name VARCHAR2,
    p_date NUMBER )
  RETURN NUMBER;

END AgingUtils;
/

create or replace
PACKAGE BODY AgingUtils
IS

/* VERSION 5.11 */
/*
edbplus ctr/ctr@'(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = 172.30.2.35)(PORT = 1521))) ( CONNECT_DATA = (SERVICE_NAME = vero)))'
*/
procedure load_defaults (prop OUT tp_prop)
IS
  properties tp_prop;
BEGIN
      properties('persistence.aggregation.range.M') :=  'M';
      properties('persistence.aggregation.range.W') :=  'W';
      properties('persistence.aggregation.range.D') := 'D';
      properties('persistence.aggregation.range.H') := 'D';
      properties('persistence.aggregation.range.MIN') :=  'D';
  ---    properties('persistence.aggregation.range.15') :=  'D';
  ---    properties('persistence.aggregation.range.01') :=  'D';
  ---    properties('persistence.aggregation.range.F_SESSION_CHUNK') :=  'D';
  prop := properties;
end;

/**
*/
function version return varchar2
is
begin
  return versionString;
end;
FUNCTION add_partition_for_table (
    p_table_name VARCHAR2,
    p_date NUMBER )
  RETURN NUMBER is
  BEGIN
  return 0;
  end;

-- more procedures and functions here ...
END AgingUtils;
/