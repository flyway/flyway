package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Properties;

import javax.sql.DataSource;

public class TeradataMigrationTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("teradata.user", "DBC");
        String password = customProperties.getProperty("teradata.password", "DBC");
        String url = customProperties.getProperty("teradata.url", "jdbc:teradata://localhost/DATABASE=DBC,CHARSET=UTF8");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void myTest() throws Exception {
        Assert.notNull(dataSource);
        Assert.notNull(flyway);

        flyway.setLocations("migration/dbsupport/teradata/");
        flyway.setAllowMixedMigrations(true);

        Assert.isTrue(flyway.migrate() == 4);
    }
}
