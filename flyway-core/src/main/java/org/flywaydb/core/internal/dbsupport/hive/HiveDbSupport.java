package org.flywaydb.core.internal.dbsupport.hive;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Support for hive metastore. It is preferable to use an Impala url to run things faster
 */
public class HiveDbSupport extends DbSupport {
    public HiveDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    public Schema getSchema(String name) {
        return new HiveShema(jdbcTemplate, this, name);
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new HiveSqlStatementBuilder();
    }

    @Override
    public String getDbName() {
        return "hive";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT current_database()");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("USE "+schema);
    }

    @Override
    public String getCurrentUserFunction() {
        return "hive";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "true";
    }

    @Override
    public String getBooleanFalse() {
        return "false";
    }

    @Override
    protected String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
