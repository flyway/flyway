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