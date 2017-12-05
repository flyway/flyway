/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.flywaydb.core.internal.database.oracle.OracleMigrationMediumTest.JDBC_PASSWORD;
import static org.flywaydb.core.internal.database.oracle.OracleMigrationMediumTest.JDBC_USER;
import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Oracle tnsnames.ora.
 */
@Category(DbCategory.Oracle.class)
public class OracleTnsnamesMediumTest {
    @Rule
    public Timeout globalTimeout = new Timeout(180, TimeUnit.SECONDS);

    @Test
    public void tnsnamesOra() throws Exception {
        System.setProperty("oracle.net.tns_admin", ClassLoader.getSystemResource("migration/database/oracle").getPath());

        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:oracle:thin:@ORACLE_10_XE", JDBC_USER, JDBC_PASSWORD);
        flyway.setSchemas("tnsnames");
        flyway.setLocations("migration/sql");
        flyway.clean();
        assertEquals(4, flyway.migrate());
    }
}