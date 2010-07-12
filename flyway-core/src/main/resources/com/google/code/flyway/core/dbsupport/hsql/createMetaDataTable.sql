CREATE TABLE ${tableName} (
    version VARCHAR(20) PRIMARY KEY,
    description VARCHAR(100),
    script VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    state VARCHAR(15) NOT NULL,
    current_version BIT NOT NULL,
    CONSTRAINT unique_script UNIQUE (script)
);
 CREATE INDEX ${tableName}_current_version_index ON ${tableName} (current_version);