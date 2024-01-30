package org.flywaydb.database.mongodb;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.AbbreviationUtils;

import java.sql.Timestamp;
import java.time.Instant;

public class MongoDBDatabase extends Database<MongoDBConnection> {

    public MongoDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected MongoDBConnection doGetConnection(java.sql.Connection connection) {
        return new MongoDBConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {

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
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "db.getSiblingDB('" + table.getSchema().getName() + "').createCollection(" + quote(table.getName()) + ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "db." + table.getName() + ".createIndex({ success: -1 })";
    }

    @Override
    public String getInsertStatement(Table table) {

        return "db.getSiblingDB('" + table.getSchema().getName() + "')." + table.getName() + ".insertOne({"
                + quote("installed_rank") + ": ?, "
                + quote("version") + ": ?, "
                + quote("description") + ": ?, "
                + quote("type") + ": ?, "
                + quote("script") + ": ?, "
                + quote("checksum") + ": ?, "
                + quote("installed_by") + ": ?, "
                + quote("installed_on") + ": '" + Timestamp.from(Instant.now()) + "', "
                + quote("execution_time") + ": ?, "
                + quote("success") + ": ?"
                + "})";
    }

    @Override
    public String getUpdateStatement(Table table) {
        return "db.getSiblingDB('" + table.getSchema().getName() + "')." + table.getName() + ".updateOne({"
                + quote("installed_rank") + ": ?"
                + "}, {"
                + "$set: {"
                + quote("description") + ": ?,"
                + quote("type") + ": ?,"
                + quote("checksum") + ": ?"
                + "}"
                + "})";
    }

    @Override
    public String getSelectStatement(Table table) {
        return "db.getSiblingDB('" + table.getSchema().getName() + "')." + table.getName() + ".find({"
                + quote("installed_rank") + ": { $gt: ? }"
                + "}).sort({"
                + quote("installed_rank") + ": 1"
                + "})";
    }

    public String getDeleteStatement(Table table, boolean version) {
        return "db.getSiblingDB('" + table.getSchema().getName() + "')." + table.getName() + ".deleteMany({ 'success': " + getBooleanFalse() + ", " +
                (version ?
                        "'version': ? " :
                        "'desciption': ? ") +
                "})";
    }

    @Override
    protected String getBaselineStatement(Table table) {
        return String.format(getInsertStatement(table).replace("?", "%s"),
                             1,
                             quoteIfNotNull(configuration.getBaselineVersion().toString()),
                             quoteIfNotNull(AbbreviationUtils.abbreviateDescription(configuration.getBaselineDescription())),
                             quoteIfNotNull(CoreMigrationType.BASELINE.toString()),
                             quoteIfNotNull(AbbreviationUtils.abbreviateScript(configuration.getBaselineDescription())),
                             null,
                             quoteIfNotNull(getInstalledBy()),
                             0,
                             getBooleanTrue()
                            );
    }

    private String quoteIfNotNull(String value) {
        return value == null ? null : quote(value);
    }
}