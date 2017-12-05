/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test for H2 in case-sensitive (DATABASE_TO_UPPER=FALSE) mode.
 */
@Category(DbCategory.H2.class)
public class H2CaseSensitiveMigrationMediumTest {
    @Test
    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db_case_sensitive;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.clean();
    }
}
