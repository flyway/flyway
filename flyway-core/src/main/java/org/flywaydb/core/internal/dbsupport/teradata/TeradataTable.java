package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;

/**
 * Teradata-specific table.
 */
public class TeradataTable extends Table {
  /**
   * Creates a new Teradata table.
   *
   * @param jdbcTemplate The Jdbc Template for communicating with the DB.
   * @param dbSupport    The database-specific support.
   * @param schema       The schema this table lives in.
   * @param name         The name of the table.
   */
  public TeradataTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
    super(jdbcTemplate, dbSupport, schema, name);
  }

  @Override
  protected void doDrop() throws SQLException {
    jdbcTemplate.execute("DROP TABLE " + dbSupport.quote(schema.getName(), name));
  }
  
  @Override
  protected boolean doExists() throws SQLException {
    return exists(null, schema, name);
  }

  @Override
  protected void doLock() throws SQLException {
    // TODO PL

  }
}
