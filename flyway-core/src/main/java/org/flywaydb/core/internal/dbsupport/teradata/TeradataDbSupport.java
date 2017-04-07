package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Teradata-specific support.
 */
public class TeradataDbSupport extends DbSupport {
  private static final Log LOG = LogFactory.getLog(TeradataDbSupport.class);

  /**
   * Creates a new instance.
   *
   * @param connection The connection to use.
   */
  public TeradataDbSupport(Connection connection) {
    super(new JdbcTemplate(connection, Types.VARCHAR));
  }

  @Override
  public String getDbName() {
    return "teradata";
  }

  @Override
  public String getCurrentUserFunction() {
    return "user";
  }

  @Override
  protected String doGetCurrentSchemaName() throws SQLException {
    return jdbcTemplate.queryForString("SELECT database");
  }

  @Override
  protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
    LOG.info("Teradata does not support schema. Default schema NOT changed to " + schema);
  }

  @Override
  public boolean supportsDdlTransactions() {
    return true;
  }

  @Override
  public String getBooleanTrue() {
    return "1";
  }

  @Override
  public String getBooleanFalse() {
    return "0";
  }

  @Override
  public SqlStatementBuilder createSqlStatementBuilder() {
    return new TeradataSqlStatementBuilder();
  }

  @Override
  public String doQuote(String identifier) {
    return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
  }

  @Override
  public Schema getSchema(String name) {
    return new TeradataSchema(jdbcTemplate, this, name);
  }

  @Override
  public boolean catalogIsSchema() {
    return false;
  }
}
