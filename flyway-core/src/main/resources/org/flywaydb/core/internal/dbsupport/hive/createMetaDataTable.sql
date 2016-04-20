-- Create table
CREATE TABLE ${schema}.${table} (
  version STRING,
  installed_rank INT,
  description STRING,
  type STRING,
  script STRING,
  checksum INT,
  installed_by STRING,
  installed_on TIMESTAMP,
  execution_time INT,
  success BOOLEAN
)
CLUSTERED BY(version) INTO 1 BUCKETS
STORED AS ORC TBLPROPERTIES ("transactional"="true");