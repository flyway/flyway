package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;

/**
 * Teradata implementation of Schema.
 */
public class TeradataSchema extends Schema<TeradataDbSupport> {
  private static final Log LOG = LogFactory.getLog(TeradataSchema.class);

  /**
   * Creates a new SQLite schema.
   *
   * @param jdbcTemplate The Jdbc Template for communicating with the DB.
   * @param dbSupport The database-specific support.
   * @param name The name of the schema.
   */
  public TeradataSchema(JdbcTemplate jdbcTemplate, TeradataDbSupport dbSupport, String name) {
    super(jdbcTemplate, dbSupport, name);
  }

  @Override
  protected boolean doExists() throws SQLException {
    return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM dbc.tables WHERE databasename=?", name) > 0;
  }

  @Override
  protected boolean doEmpty() throws SQLException {
    // TODO PL
    return false;
  }

  @Override
  protected void doCreate() throws SQLException {
    LOG.info("Teradata does not support creating schemas. Schema not created: " + name);
  }

  @Override
  protected void doDrop() throws SQLException {
    LOG.info("Teradata does not support dropping schemas. Schema not dropped: " + name);
  }

  @Override
  protected void doClean() throws SQLException {
    // TODO PL
  }

  @Override
  protected Table[] doAllTables() throws SQLException {
    // TODO PL
    return null;
  }

  @Override
  public Table getTable(String tableName) {
    return new TeradataTable(jdbcTemplate, dbSupport, this, tableName);
  }
}
