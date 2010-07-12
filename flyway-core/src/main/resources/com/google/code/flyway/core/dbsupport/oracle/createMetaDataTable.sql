CREATE TABLE ${tableName} (
    version VARCHAR2(20) NOT NULL PRIMARY KEY,
    description VARCHAR2(100),
    script VARCHAR2(100) NOT NULL UNIQUE,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    state VARCHAR2(15) NOT NULL,
    current_version NUMBER(1) NOT NULL
);
CREATE INDEX ${tableName}_cv_idx ON ${tableName} (current_version);