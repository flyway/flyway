package com.google.code.flyway.core;

import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Utility for creating and dropping Oracle database schemas and users.
 */
public class OracleDbCreator {
@Autowired
    @Qualifier("root-simpleJdbcTemplate")
    private SimpleJdbcTemplate rootSimpleJdbcTemplate;

    @PostConstruct
    public void createDatabase() {
        //MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/oracle/createDatabase.sql"));
    }

    @PreDestroy
    public void dropDatabase() {
        //MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/oracle/dropDatabase.sql"));
    }
}
