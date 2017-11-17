package org.flywaydb.core.internal.util.jdbc.pro;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.util.DateUtils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

public class DryRunStatementInterceptor implements Closeable {
    private static final Log LOG = LogFactory.getLog(DryRunStatementInterceptor.class);

    private final Writer dryRunOutput;
    private DbSupport dbSupport;
    private Table table;
    private String delimiterStr;

    public static DryRunStatementInterceptor of(File dryRunOutput, String encoding) {
        File file;
        try {
            file = dryRunOutput.getCanonicalFile();
        } catch (IOException e) {
            throw new FlywayException("Unable to get canonical path for dry run output "
                    + dryRunOutput.getAbsolutePath() + ": " + e.getMessage(), e);
        }
        String path = file.getAbsolutePath();
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new FlywayException("Unable to write dry run output to " + path + " as it is a directory and not a file");
            }
            if (!file.canWrite()) {
                throw new FlywayException("Unable to write dry run output to " + path + " as it is write-protected");
            }
            LOG.warn("Overwriting existing dry run out file " + path + " ...");
        } else {
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new FlywayException("Unable to create parent directories for dry run output to " + path);
                }
            }
        }

        try {
            return new DryRunStatementInterceptor(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), encoding)));
        } catch (IOException e) {
            throw new FlywayException("Unable to write dry run output to " + path + ": " + e.getMessage(), e);
        }
    }

    DryRunStatementInterceptor(Writer writer) {
        this.dryRunOutput = writer;
        append("-- Flyway Dry Run (" + DateUtils.formatDateAsIsoString(new Date()) + ")");
        append("--------------------------------------- ");
    }

    public void init(DbSupport dbSupport, Table table) {
        this.dbSupport = dbSupport;
        this.table = table;
        Delimiter defaultDelimiter = dbSupport.getDefaultDelimiter();
        this.delimiterStr = (defaultDelimiter.isAloneOnLine() ? "\n" : "") + defaultDelimiter.getDelimiter();
    }

    public void schemaHistoryTableCreate() {
        append(dbSupport.getCreateScript(table));
    }

    public void schemaHistoryTableInsert(AppliedMigration appliedMigration) {
        String insertStatement = String.format(
                dbSupport.getInsertStatement(table).replace("?", "%s"),
                appliedMigration.getInstalledRank(),
                "'" + appliedMigration.getVersion() + "'",
                "'" + appliedMigration.getDescription() + "'",
                "'" + appliedMigration.getType() + "'",
                "'" + appliedMigration.getScript() + "'",
                appliedMigration.getChecksum(),
                "'" + appliedMigration.getInstalledBy() + "'",
                appliedMigration.getExecutionTime(),
                appliedMigration.isSuccess() ? dbSupport.getBooleanTrue() : dbSupport.getBooleanFalse());
        append(insertStatement);
    }

    public void interceptStatement(String sql) {
        append(sql + delimiterStr);
    }

    public void interceptPreparedStatement(String sql) {
        append(sql + delimiterStr);
    }

    public void interceptCallableStatement(String sql) {
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
}
