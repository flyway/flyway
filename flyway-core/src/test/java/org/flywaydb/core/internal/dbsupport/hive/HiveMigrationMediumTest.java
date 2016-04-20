package org.flywaydb.core.internal.dbsupport.hive;

import com.google.common.io.Files;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.com.esotericsoftware.kryo.util.Util;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@Category(DbCategory.Hive.class)
public class HiveMigrationMediumTest extends MigrationTestCase {


    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), "org.flywaydb.core.internal.dbsupport.hive.driver.HiveProxyDriver", "jdbc:hive2:///", null, null);
    }

    @Override
    protected String getMigrationDir() {
        return "migration/dbsupport/hive/sql";
    }


    @Override
    protected String getBasedir() { return getMigrationDir() + "/sql"; }

    @Override
    protected String getQuoteLocation() { return getMigrationDir() + "/quote"; }

    @Override
    protected String getFutureFailedLocation() { return getMigrationDir() + "/future_failed"; }


    @Override
    protected String getSemiColonLocation() { return getMigrationDir() + "/semicolon"; }

    @Override
    protected String getCommentLocation() { return getMigrationDir() + "/comment"; }

    @Override
    protected String getValidateLocation() { return getMigrationDir() + "/validate"; }

    @Override
    protected void createTestTable() throws SQLException {
        jdbcTemplate.execute("CREATE TABLE t1 (name VARCHAR(25))");
    }

    @Override
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations(getSemiColonLocation());
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        // Ignored test:
        // Hive does not seem to support multiline values, see https://issues.apache.org/jira/browse/HIVE-5999
        // we end up with 2 different rows...
        /*assertEquals("Mr. Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("select name from test_user where name like '%line'"));*/

    }

    @Ignore("Not needed as Hive support was first introduced in Flyway 4.0")
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {}
}
