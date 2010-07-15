package com.google.code.flyway.sample.migration;

import com.google.code.flyway.core.dbsupport.DbSupport;
import com.google.code.flyway.core.java.BaseJavaMigration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Example of a Java-based migration.
 */
public class V1_2__Another_user extends BaseJavaMigration {
    @Override
    protected void doMigrateInTransaction(JdbcTemplate jdbcTemplate, DbSupport dbSupport) throws DataAccessException {
        jdbcTemplate.execute("INSERT INTO test_user (name) VALUES ('Obelix')");
    }
}
