CREATE TABLE ${tableName} (
    version VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100),
    script VARCHAR(100) NOT NULL UNIQUE,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    state VARCHAR(15) NOT NULL,
    current_version BOOL NOT NULL,
    PRIMARY KEY(version)
) ENGINE=InnoDB";
ALTER TABLE ${tableName} ADD INDEX ${tableName}_current_version_index (current_version);