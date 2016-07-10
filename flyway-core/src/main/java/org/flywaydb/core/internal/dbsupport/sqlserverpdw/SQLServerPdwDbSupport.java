/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.flywaydb.core.internal.dbsupport.sqlserverpdw;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.sqlserver.SQLServerDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlserver.SQLServerSqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 *
 * @author fdefalco
 */
public class SQLServerPdwDbSupport extends DbSupport {

  private static final Log LOG = LogFactory.getLog(SQLServerDbSupport.class);
  private String username = "not available";
  
  /**
   * Creates a new instance.
   *
   * @param connection The connection to use.
   */
  public SQLServerPdwDbSupport(Connection connection) {
    super(new JdbcTemplate(connection, Types.VARCHAR));
    try {
    username = jdbcTemplate.queryForString("SELECT SUSER_SNAME()");
    } catch (SQLException ex) {
      LOG.debug("error loading username from " + getDbName() + " : " + ex.getMessage());
    }
  }

  public String getDbName() {
    return "sqlserverpdw";
  }

  public String getCurrentUserFunction() {
    return "'" + username + "'";   
  }

  @Override
  protected String doGetCurrentSchemaName() throws SQLException {
    return jdbcTemplate.queryForString("SELECT SCHEMA_NAME()");
  }

  @Override
  protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
    LOG.info("SQLServer Parallel Data Warehouse does not support setting the schema for the current session. Default schema NOT changed to " + schema);
    // Not currently supported.
    // See http://connect.microsoft.com/SQLServer/feedback/details/390528/t-sql-statement-for-changing-default-schema-context
  }

  public boolean supportsDdlTransactions() {
    return false;
  }

  public String getBooleanTrue() {
    return "1";
  }

  public String getBooleanFalse() {
    return "0";
  }

  public SqlStatementBuilder createSqlStatementBuilder() {
    return new SQLServerSqlStatementBuilder();
  }

  /**
   * Escapes this identifier, so it can be safely used in sql queries.
   *
   * @param identifier The identifier to escaped.
   * @return The escaped version.
   */
  private String escapeIdentifier(String identifier) {
    return StringUtils.replaceAll(identifier, "]", "]]");
  }

  @Override
  public String doQuote(String identifier) {
    return "[" + escapeIdentifier(identifier) + "]";
  }

  @Override
  public Schema getSchema(String name) {
    return new SQLServerPdwSchema(jdbcTemplate, this, name);
  }

  @Override
  public boolean catalogIsSchema() {
    return false;
  }
}
