package com.googlecode.flyway.core.init;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.AppliedMigration;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles Flyway's init command.
 */
public class DbInit {
    private static final Log LOG = LogFactory.getLog(DbInit.class);

    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The version to tag an existing schema with when executing init.
     */
    private final MigrationVersion initVersion;

    /**
     * The description to tag an existing schema with when executing init.
     */
    private final String initDescription;

    /**
     * Creates a new DbInit.
     *
     * @param connection      The database connection to use for accessing the metadata table.
     * @param metaDataTable   The metadata table.
     * @param initVersion     The version to tag an existing schema with when executing init.
     * @param initDescription The description to tag an existing schema with when executing init.
     */
    public DbInit(Connection connection, MetaDataTable metaDataTable, MigrationVersion initVersion, String initDescription) {
        this.connection = connection;
        this.metaDataTable = metaDataTable;
        this.initVersion = initVersion;
        this.initDescription = initDescription;
    }

    /**
     * Initializes the database.
     */
    public void init() {
        try {
             new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
                 public Void doInTransaction() {
                     List<AppliedMigration> appliedMigrations = metaDataTable.allAppliedMigrations();
                     if (appliedMigrations.isEmpty()
                             || ((appliedMigrations.size() == 1) && (appliedMigrations.get(0).getType() == MigrationType.SCHEMA))) {
                         metaDataTable.init(initVersion, initDescription);
                         return null;
                     }
                     throw new FlywayException(
                             "Schema already initialized. Current Version: " + metaDataTable.getCurrentSchemaVersion());
                 }
             });
         } catch (SQLException e) {
             throw new FlywayException("Error initializing metadata table " + metaDataTable, e);

         }

         LOG.info("Schema initialized with version: " + initVersion);
    }
}
