package org.flywaydb.core.internal.util.jdbc.pro;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.util.DateUtils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

public class DryRunStatementInterceptor implements Closeable {
    private static final Log LOG = LogFactory.getLog(DryRunStatementInterceptor.class);

    private final Writer dryRunOutput;
    private Database database;
    private Table table;
    private String delimiterStr;

    public DryRunStatementInterceptor(OutputStream dryRunOutput, String encoding) {
        try {
            this.dryRunOutput = new BufferedWriter(new OutputStreamWriter(dryRunOutput, encoding));
        } catch (IOException e) {
            throw new FlywayException("Unable to write dry run output: " + e.getMessage(), e);
        }
        append("---====================================");
        append("-- Flyway Dry Run (" + DateUtils.formatDateAsIsoString(new Date()) + ")");
        append("---====================================");
        append("");
    }

    public void init(Database database, Table table) {
        this.database = database;
        this.table = table;
        Delimiter defaultDelimiter = database.getDefaultDelimiter();
        this.delimiterStr = (defaultDelimiter.isAloneOnLine() ? "\n" : "") + defaultDelimiter.getDelimiter();
    }

    public void schemaHistoryTableCreate() {
        append(database.getCreateScript(table));
    }

    public void schemaHistoryTableInsert(AppliedMigration appliedMigration) {
        String insertStatement = String.format(
                database.getInsertStatement(table).replace("?", "%s"),
                appliedMigration.getInstalledRank(),
                "'" + appliedMigration.getVersion() + "'",
                "'" + appliedMigration.getDescription() + "'",
                "'" + appliedMigration.getType() + "'",
                "'" + appliedMigration.getScript() + "'",
                appliedMigration.getChecksum(),
                "'" + appliedMigration.getInstalledBy() + "'",
                appliedMigration.getExecutionTime(),
                appliedMigration.isSuccess() ? database.getBooleanTrue() : database.getBooleanFalse());
        append(insertStatement);
    }

    void interceptStatement(String sql) {
        append(sql + delimiterStr);
    }

    void interceptPreparedStatement(String sql) {
        append(sql + delimiterStr);
    }

    void interceptCallableStatement(String sql) {
        append(sql + delimiterStr);
    }

    private void append(String sql) {
        try {
            dryRunOutput.write(sql + "\n");
        } catch (IOException e) {
            throw new FlywayException("Unable to write to dry run output: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            dryRunOutput.close();
        } catch (IOException e) {
            LOG.warn("Unable to close dry run output: " + e.getMessage());
        }
    }

    void interceptCommand(String command) {
        append("");
        append("-- Executing: " + command + " (with callbacks)");
        append("------------------------------------------------------------------------------------------");
    }
}
