/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SchemaObjectType;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchema.OracleSchemaObjectTypeWithPrefetchedObjects.withPrefetchedObjects;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.CLUSTER;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.DOMAIN_INDEX;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.FUNCTION;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.INDEX;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.JAVA_SOURCE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.MATERIALIZED_VIEW;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.MATERIALIZED_VIEW_LOG;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.PACKAGE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.PROCEDURE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.QUEUE_TABLE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.SCHEDULER_JOB;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.SEQUENCE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.SYNONYM;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.TABLE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.TRIGGER;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.TYPE;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.VIEW;
import static org.flywaydb.core.internal.dbsupport.oracle.OracleSchemaObjectType.XML_TABLE;


/**
 * Oracle implementation of Schema.
 */
public class OracleSchema extends Schema<OracleDbSupport> {
    private static final Log LOG = LogFactory.getLog(OracleSchema.class);

    /**
     * Creates a new Oracle schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public OracleSchema(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    /**
     * Checks whether the schema is system, i.e. Oracle-maintained, or not.
     *
     * @return {@code true} if it is system, {@code false} if not.
     */
    protected boolean isSystem() throws SQLException {
        return dbSupport.getSystemSchemas().contains(name);
    }

    /**
     * Checks whether this schema is default for the current user.
     *
     * @return {@code true} if it is default, {@code false} if not.
     */
    protected boolean isDefaultSchemaForUser() throws SQLException {
        return name.equals(dbSupport.getCurrentUserName());
    }

    @Override
    protected boolean doExists() throws SQLException {
        return dbSupport.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = ?", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return !dbSupport.queryReturnsRows("SELECT * FROM ALL_OBJECTS WHERE OWNER = ?", name);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + dbSupport.quote(name) + " IDENTIFIED BY flyway");
        jdbcTemplate.execute("GRANT RESOURCE TO " + dbSupport.quote(name));
        jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if (isSystem()) {
            throw new FlywayException("Clean not supported on Oracle for system schema " + dbSupport.quote(name) + "! " +
                    "It must not be changed in any way except by running an Oracle-supplied script!");
        }

        // Disable FBA for schema tables
        if (dbSupport.isFlashbackDataArchiveAvailable()) {
            disableFlashbackArchiveForFbaTrackedTables();
        }

        // Cleanup Oracle Locator metadata
        if (dbSupport.isLocatorAvailable()) {
            cleanLocatorMetadata();
        }

        // Take a snapshot of existing objects grouped by type. This map is used for 2 goals:
        //   1. if the type is not present in the map, skip dropping;
        //   2. types that cannot get cascadingly dropped by other objects may use it as a cached list of objects to drop.
        Map<String, List<String>> objectsByType = getObjectsGroupedByType();

        // Ordered list of types to be dropped (some can be provided with a pre-fetched list of objects).
        @SuppressWarnings("unchecked")
        List<SchemaObjectType<OracleSchema>> objectTypesToDrop = Arrays.asList(
                withPrefetchedObjects(TRIGGER, objectsByType),
                QUEUE_TABLE,
                withPrefetchedObjects(SCHEDULER_JOB, objectsByType),
                withPrefetchedObjects(MATERIALIZED_VIEW, objectsByType),
                MATERIALIZED_VIEW_LOG,
                VIEW,
                DOMAIN_INDEX,
                XML_TABLE,
                TABLE,
                INDEX,
                withPrefetchedObjects(CLUSTER, objectsByType),
                SEQUENCE,
                withPrefetchedObjects(FUNCTION, objectsByType),
                withPrefetchedObjects(PROCEDURE, objectsByType),
                withPrefetchedObjects(PACKAGE, objectsByType),
                TYPE,
                withPrefetchedObjects(SYNONYM, objectsByType),
                withPrefetchedObjects(JAVA_SOURCE, objectsByType)
                // TODO: add more types
        );

        for (SchemaObjectType<OracleSchema> type : objectTypesToDrop) {
            if (objectsByType.containsKey(type.getName())) {
                for (String objectName : type.getObjectNames(this)) {
                    type.dropObject(this, objectName);
                }
            }
        }

        if (isDefaultSchemaForUser()) {
            jdbcTemplate.execute("PURGE RECYCLEBIN");
        }

    }

    /**
     * Returns the schema's objects grouped in lists by object type (value of ALL_OBJECTS.OBJECT_TYPE column).
     * @return a map of type names to object name lists
     * @throws SQLException if retrieving of objects failed
     */
    private Map<String, List<String>> getObjectsGroupedByType() throws SQLException {
        boolean xmlDbAvailable = dbSupport.isXmlDbAvailable();
        String query =
                // Most of objects are seen in ALL_OBJECTS
                "SELECT OBJECT_TYPE, OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? " +
                        (xmlDbAvailable
                                // XML tables are seen in a separate dictionary table
                                ? "UNION ALL SELECT 'TABLE', TABLE_NAME FROM ALL_XML_TABLES WHERE OWNER = ? " +
                                "AND TABLE_NAME NOT LIKE 'BIN$________________________$_'" //ignore recycle bin objects
                                : "");

        // Count params
        int n = 1;
        if (xmlDbAvailable) n += 1;
        String[] params = new String[n];
        Arrays.fill(params, name);

        List<Map<String, String>> rows = jdbcTemplate.queryForList(query, params);
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (Map<String, String> row : rows) {
            String objectType = row.get("OBJECT_TYPE");
            String objectName = row.get("OBJECT_NAME");
            if (result.containsKey(objectType)) {
                result.get(objectType).add(objectName);
            } else {
                List<String> newList = new ArrayList<String>();
                newList.add(objectName);
                result.put(objectType, newList);
            }
        }
        return result;
    }

    /**
     * Executes ALTER statements for all tables that have Flashback Archive enabled.
     * Flashback Archive is an asynchronous process so we need to wait until it completes, otherwise cleaning the
     * tables in schema will sometimes fail with ORA-55622 or ORA-55610 depending on the race between
     * Flashback Archive and Java code.
     *
     * @throws SQLException when the statements could not be generated.
     */
    private void disableFlashbackArchiveForFbaTrackedTables() throws SQLException {
        // DBA_FLASHBACK_ARCHIVE_TABLES is granted to PUBLIC
        String queryForFbaTrackedTables = "SELECT TABLE_NAME FROM DBA_FLASHBACK_ARCHIVE_TABLES WHERE OWNER_NAME = ?";
        List<String> tableNames = jdbcTemplate.queryForStringList(queryForFbaTrackedTables, name);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE " + dbSupport.quote(name, tableName) + " NO FLASHBACK ARCHIVE");
            //wait until the tables disappear
            while (dbSupport.queryReturnsRows(queryForFbaTrackedTables + " AND TABLE_NAME = ?", name, tableName)) {
                try {
                    LOG.debug("Actively waiting for Flashback cleanup on table: " + dbSupport.quote(name, tableName));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
                }
            }
        }
    }

    /**
     * Checks whether Oracle Locator metadata exists for the schema.
     *
     * @return {@code true} if it exists, {@code false} if not.
     * @throws SQLException when checking checking metadata existence failed.
     */
    private boolean locatorMetadataExists() throws SQLException {
        return dbSupport.queryReturnsRows("SELECT * FROM ALL_SDO_GEOM_METADATA WHERE OWNER = ?", name);
    }

    /**
     * Clean Oracle Locator metadata for the schema. Works only for the user's default schema, prints a warning message
     * to log otherwise.
     *
     * @throws SQLException when performing cleaning failed.
     */
    private void cleanLocatorMetadata() throws SQLException {
        if (!locatorMetadataExists()) {
            return;
        }

        if (!isDefaultSchemaForUser()) {
            LOG.warn("Unable to clean Oracle Locator metadata for schema " + dbSupport.quote(name) +
                    " by user \"" + dbSupport.getCurrentUserName() + "\": unsupported operation");
            return;
        }

        jdbcTemplate.getConnection().commit();
        jdbcTemplate.execute("DELETE FROM USER_SDO_GEOM_METADATA");
        jdbcTemplate.getConnection().commit();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = TABLE.getObjectNames(this);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new OracleTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new OracleTable(jdbcTemplate, dbSupport, this, tableName);
    }

    /**
     * Exposes jdbcTemplate to OracleSchemaObjectType
     */
    JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Exposes dbSupport to OracleSchemaObjectType
     */
    OracleDbSupport getDbSupport() {
        return dbSupport;
    }


    /**
     * A decorator of OracleSchemaObjectType that takes a pre-fetched list of objects to be dropped.
     * @see #doClean()
     */
    static class OracleSchemaObjectTypeWithPrefetchedObjects implements SchemaObjectType<OracleSchema> {

        private final OracleSchemaObjectType objectType;
        private final List<String> objectNames;

        private OracleSchemaObjectTypeWithPrefetchedObjects(OracleSchemaObjectType objectType, List<String> objectNames) {
            this.objectType = objectType;
            this.objectNames = objectNames;
        }

        static OracleSchemaObjectTypeWithPrefetchedObjects withPrefetchedObjects(OracleSchemaObjectType objectType,
                                                                             Map<String, List<String>> objectsByType) {
            return withPrefetchedObjects(
                    objectType,
                    objectsByType.containsKey(objectType.getName())
                            ? objectsByType.get(objectType.getName())
                            : Collections.<String>emptyList()
            );
        }

        static OracleSchemaObjectTypeWithPrefetchedObjects withPrefetchedObjects(OracleSchemaObjectType objectType,
                                                                             List<String> objectNames) {
            return new OracleSchemaObjectTypeWithPrefetchedObjects(objectType, objectNames);
        }

        @Override
        public String getName() {
            return objectType.getName();
        }

        @Override
        public List<String> getObjectNames(OracleSchema schema) throws SQLException {
            return objectNames;
        }

        @Override
        public String generateDropStatement(OracleSchema schema, String objectName) {
            return objectType.generateDropStatement(schema, objectName);
        }

        @Override
        public void dropObject(OracleSchema schema, String objectName) throws SQLException {
            objectType.dropObject(schema, objectName);
        }
    }
}
