package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Test;

/**
 * Test for H2 in case-sensitive (DATABASE_TO_UPPER=FALSE) mode.
 */
public class H2CaseSensitiveMigrationMediumTest {
    @Test
    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(new DriverDataSource(null, "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", "sa", ""));
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.clean();
    }
}
