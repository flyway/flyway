package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

public class TeradataMigrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationTestCase.class);

  protected DataSource dataSource;
  private Connection connection;
  protected DbSupport dbSupport;

  protected JdbcTemplate jdbcTemplate;
  protected Flyway flyway;
  
  @Before
  public void setUp() throws Exception {
      File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
      Properties customProperties = new Properties();
      if (customPropertiesFile.canRead()) {
          customProperties.load(new FileInputStream(customPropertiesFile));
      }
      dataSource = createDataSource(customProperties);

      connection = dataSource.getConnection();
      dbSupport = DbSupportFactory.createDbSupport(connection, false);
      jdbcTemplate = dbSupport.getJdbcTemplate();

      configureFlyway();
      // flyway.clean();
  }

  protected void configureFlyway() {
      flyway = new Flyway();
      flyway.setDataSource(dataSource);
  }
  
  protected DataSource createDataSource(Properties customProperties) throws Exception {
    String user = customProperties.getProperty("teradata.user", "DBC");
    String password = customProperties.getProperty("teradata.password", "DBC");
    String url = customProperties.getProperty("teradata.url", "jdbc:teradata://localhost/DATABASE=DBC,CHARSET=UTF8");

    return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
  }

  protected String getQuoteLocation() {
    return "migration/quote";
  }

  @Test
  public void myTest() throws Exception {
    Assert.notNull(dataSource);
    Assert.notNull(flyway);

    flyway.setBaselineOnMigrate(true);
    flyway.setLocations("migration/dbsupport/teradata/");
    flyway.migrate();
  }
}
