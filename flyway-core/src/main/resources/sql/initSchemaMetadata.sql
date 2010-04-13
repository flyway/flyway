CREATE TABLE schema_version (
    major INT NOT NULL,
    minor INT NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
	
CREATE TABLE schema_maintenance_history (
    id INT NOT NULL AUTO_INCREMENT,
	script VARCHAR(100) NOT NULL UNIQUE,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id)
) ENGINE=InnoDB;
